package org.example.hubs_brokerage;

import org.example.hubs_brokerage.dto.AssetResponse;
import org.example.hubs_brokerage.dto.OrderRequest;
import org.example.hubs_brokerage.dto.OrderResponse;
import org.example.hubs_brokerage.exception.InsufficientBalanceException;
import org.example.hubs_brokerage.exception.ResourceNotFoundException;
import org.example.hubs_brokerage.model.*;
import org.example.hubs_brokerage.repository.OrderRepository;
import org.example.hubs_brokerage.service.AssetService;
import org.example.hubs_brokerage.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private OrderService orderService;

    private final String customerId = "customer123";
    private final String assetName = "TRY";
    private final double size = 3.0;
    private final double price = 10.0;
    private OrderRequest buyRequest;
    private OrderRequest sellRequest;

    @BeforeEach
    void setUp() {
        buyRequest = new OrderRequest(customerId, assetName, OrderSide.BUY, size, price);
        sellRequest = new OrderRequest(customerId, assetName, OrderSide.SELL, size, price);
    }

    @Test
    void createOrder_WithSufficientBalance_ShouldCreateBuyOrder() {
        // Arrange
        when(assetService.getAvailableBalance(customerId, "TRY")).thenReturn(2000.0);

        Order expectedOrder = new Order(customerId, assetName, OrderSide.BUY, size, price);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order result = orderService.createOrder(buyRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderSide.BUY, result.getSide());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetService).reserveAsset(customerId, "TRY", size * price);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        when(assetService.getAvailableBalance(customerId, "TRY")).thenReturn(1000.0);

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(buyRequest);
        });
    }

    @Test
    void createOrder_WithSufficientAssets_ShouldCreateSellOrder() {
        // Arrange
        when(assetService.getAvailableBalance(customerId, assetName)).thenReturn(size);

        Order expectedOrder = new Order(customerId, assetName, OrderSide.SELL, size, price);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order result = orderService.createOrder(sellRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderSide.SELL, result.getSide());
        verify(assetService).reserveAsset(customerId, assetName, size);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_WithPendingStatus_ShouldCancelOrder() {
        // Arrange
        Order pendingOrder = new Order(customerId, assetName, OrderSide.BUY, size, price);
        pendingOrder.setId(1L);
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, pendingOrder.getStatus());
        verify(orderRepository).save(pendingOrder);
        // verify(assetService).refundAsset(...); // Refund işlemi yorum satırında olduğu için test edilmiyor
    }

    @Test
    void cancelOrder_WithNonPendingStatus_ShouldThrowException() {
        // Arrange
        Order matchedOrder = new Order(customerId, assetName, OrderSide.BUY, size, price);
        matchedOrder.setId(1L);
        matchedOrder.setStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(matchedOrder));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(1L);
        });
    }

    @Test
    void getOrderById_WithExistingOrder_ShouldReturnOrder() {
        // Arrange
        Order expectedOrder = new Order(customerId, assetName, OrderSide.BUY, size, price);
        expectedOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));

        // Act
        Order result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderById_WithNonExistingOrder_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(1L);
        });
    }

    @Test
    void getCustomerOrders_ShouldReturnPaginatedResults() {
        // Arrange
        Order order = new Order(customerId, assetName, OrderSide.BUY, size, price);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findFilteredOrders(
                customerId,
                null,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                pageable))
                .thenReturn(orderPage);

        // Act
        Page<OrderResponse> result = orderService.getCustomerOrders(
                customerId, null, null, null, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findFilteredOrders(
                customerId, null, LocalDateTime.MIN, LocalDateTime.MAX, pageable);
    }

    @Test
    void matchBuyOrder_WithSufficientFunds_ShouldMatchOrder() {
        // Arrange
        Order buyOrder = new Order(customerId, assetName, OrderSide.BUY, size, price);
        buyOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(buyOrder));
        when(assetService.getAssetDetails(customerId, "TRY"))
                .thenReturn(new AssetResponse("TRY", 2000.0, 2000.0, 0.0, 100));
        when(assetService.getAssetDetails(customerId, assetName))
                .thenReturn(new AssetResponse(assetName, 0.0, 0.0,0.0, 0));

        // Act
        orderService.matchOrder(1L);

        // Assert
        assertEquals(OrderStatus.MATCHED, buyOrder.getStatus());
        verify(assetService).reserveAsset(customerId, "TRY", size * price);
        verify(assetService).refundAsset(customerId, assetName, size);
        verify(orderRepository).save(buyOrder);
    }

    @Test
    void matchSellOrder_WithSufficientAssets_ShouldMatchOrder() {
        // Arrange
        Order sellOrder = new Order(customerId, assetName, OrderSide.SELL, size, price);
        sellOrder.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sellOrder));
        when(assetService.getAssetDetails(customerId, assetName))
                .thenReturn(new AssetResponse(assetName, size, size, size, size));
        when(assetService.getAssetDetails(customerId, "TRY"))
                .thenReturn(new AssetResponse("TRY", 0.0, 0.0, 0.0,0));

        // Act
        orderService.matchOrder(1L);

        // Assert
        assertEquals(OrderStatus.MATCHED, sellOrder.getStatus());
        verify(assetService).reserveAsset(customerId, assetName, size);
        verify(assetService).refundAsset(customerId, "TRY", size * price);
        verify(orderRepository).save(sellOrder);
    }

    @Test
    void validateOrder_WithSufficientBalance_ShouldNotThrowException() {
        // Arrange
        when(assetService.getAvailableBalance(customerId, "TRY")).thenReturn(2000.0);

        // Act & Assert
        assertDoesNotThrow(() -> {
            orderService.validateOrder(buyRequest);
        });
    }

    @Test
    void validateOrder_WithInsufficientBalance_ShouldThrowException() {
        // Arrange
        when(assetService.getAvailableBalance(customerId, "TRY")).thenReturn(1000.0);

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.validateOrder(buyRequest);
        });
    }
}