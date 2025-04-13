package org.example.hubs_brokerage.service;

import org.example.hubs_brokerage.dto.AssetResponse;
import org.example.hubs_brokerage.dto.OrderRequest;
import org.example.hubs_brokerage.dto.OrderResponse;
import org.example.hubs_brokerage.exception.InsufficientBalanceException;
import org.example.hubs_brokerage.exception.ResourceNotFoundException;
import org.example.hubs_brokerage.model.Asset;
import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderSide;
import org.example.hubs_brokerage.model.OrderStatus;
import org.example.hubs_brokerage.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;

    public OrderService(OrderRepository orderRepository, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        validateBalance(request);

        // Varlıkları rezerve et
        String assetName = request.side() == OrderSide.BUY ? "TRY" : request.assetName();
        double amount = request.side() == OrderSide.BUY ?
                request.price() * request.size() :
                request.size();

        assetService.reserveAsset(request.customerId(), assetName, amount);

        Order order = new Order(
                request.customerId(),
                request.assetName(),
                request.side(),
                request.size(),
                request.price()
        );

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        //assetService.refundAsset(); // İptal durumunda varlıkları iade et
    }

    public void validateOrder(OrderRequest request) {
        String assetName = request.side() == OrderSide.BUY ? "TRY" : request.assetName();
        double requiredAmount = request.side() == OrderSide.BUY ?
                request.price() * request.size() :
                request.size();

        double availableBalance = assetService.getAvailableBalance(
                request.customerId(),
                assetName
        );

        if (availableBalance < requiredAmount) {
            throw new InsufficientBalanceException(
                    "Yetersiz bakiye. Gerekli: " + requiredAmount + ", Mevcut: " + availableBalance
            );
        }
    }

    private void validateBalance(OrderRequest request) {
        double requiredAmount = request.price() * request.size();

        double availableBalance = assetService.getAvailableBalance(
                request.customerId(),
                request.side() == OrderSide.BUY ? "TRY" : request.assetName()
        );


        if (availableBalance < requiredAmount) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Required: %.2f, Available: %.2f",
                            requiredAmount, availableBalance)
            );
        }
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public Page<OrderResponse> getCustomerOrders(String customerId, OrderStatus status,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate,
                                                 Pageable pageable) {

        Page<Order> orders = orderRepository.findFilteredOrders(
                customerId,
                status,
                startDate != null ? startDate : LocalDateTime.MIN,
                endDate != null ? endDate : LocalDateTime.MAX,
                pageable
        );

        return orders.map(OrderResponse::fromEntity);
    }

    public List<AssetResponse> listAssets(String customerId) {
        //List<Asset> assets = assetRepository.findByCustomerId(customerId);
        List<Asset> assets = new ArrayList<>();

        return assets.stream()
                .map(AssetResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order status is not pending");
        }

        if (order.getSide() == OrderSide.BUY) {
            matchBuyOrder(order);
        } else if (order.getSide() == OrderSide.SELL) {
            matchSellOrder(order);
        } else {
            throw new RuntimeException("Invalid order side");
        }
    }

    @Transactional
    public void matchBuyOrder(Order buyOrder) {
        // TRY varlığını al
        AssetResponse tryAssetResponse = assetService.getAssetDetails(buyOrder.getCustomerId(), "TRY");
        //Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), "TRY").orElseThrow(() -> new RuntimeException("TRY Varlığı bulunamadı"));


        // Yeterli TRY kontrolü ve rezervasyonu
        double requiredTry = buyOrder.getSize() * buyOrder.getPrice();
        if (tryAssetResponse.usableSize() < requiredTry) {
            throw new RuntimeException("Insufficient TRY balance");
        }
        assetService.reserveAsset(buyOrder.getCustomerId(), "TRY", requiredTry);

        // Alınacak varlığı al ve güncelle
        AssetResponse boughtAssetResponse = assetService.getAssetDetails(buyOrder.getCustomerId(), buyOrder.getAssetName());
        // Asset boughtAsset = assetRepository.findByCustomerIdAndAssetName(buyOrder.getCustomerId(), buyOrder.getAssetName()).orElseThrow(() -> new RuntimeException("Alınacak varlık bulunamadı"));
        assetService.refundAsset(buyOrder.getCustomerId(), buyOrder.getAssetName(), buyOrder.getSize());

        // Siparişi tamamla
        buyOrder.setStatus(OrderStatus.MATCHED);
        orderRepository.save(buyOrder);
    }

    @Transactional
    public void matchSellOrder(Order sellOrder) {
        // Satılacak varlığı al
        AssetResponse soldAssetResponse = assetService.getAssetDetails(sellOrder.getCustomerId(), sellOrder.getAssetName());
        //Asset soldAsset = assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), sellOrder.getAssetName()).orElseThrow(() -> new RuntimeException("Satılacak varlık bulunamadı"));


        // Check and reserve sufficient asset
        double sellSize = sellOrder.getSize();
        if (soldAssetResponse.usableSize() < sellSize) {
            throw new RuntimeException("Insufficient asset balance");
        }
        assetService.reserveAsset(sellOrder.getCustomerId(), sellOrder.getAssetName(), sellSize);

        // Update TRY asset
        double receivedTry = sellOrder.getSize() * sellOrder.getPrice();
        AssetResponse tryAssetResponse = assetService.getAssetDetails(sellOrder.getCustomerId(), "TRY");
        // Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(sellOrder.getCustomerId(), "TRY").orElseThrow(() -> new RuntimeException("TRY Varlığı bulunamadı"));
        assetService.refundAsset(sellOrder.getCustomerId(), "TRY", receivedTry);

        // Complete the order
        sellOrder.setStatus(OrderStatus.MATCHED);
        orderRepository.save(sellOrder);
    }
}
