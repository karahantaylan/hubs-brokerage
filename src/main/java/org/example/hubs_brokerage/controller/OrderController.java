package org.example.hubs_brokerage.controller;

import jakarta.validation.Valid;
import org.example.hubs_brokerage.dto.AssetResponse;
import org.example.hubs_brokerage.dto.OrderRequest;
import org.example.hubs_brokerage.dto.OrderResponse;
import org.example.hubs_brokerage.model.Asset;
import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderStatus;
import org.example.hubs_brokerage.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        Order createdOrder = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
/*
    @GetMapping
    public ResponseEntity<Page<Order>> listOrders(
            @RequestParam String customerId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(customerId, status, pageable));
    }
*/
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> listAssets(
            @RequestParam String customerId,
            @RequestHeader("Authorization") String authHeader) {

        // Örnek: Yetki kontrolü
        if (!authHeader.startsWith("Basic ")) {
            throw new SecurityException("Unauthorized access");
        }

        return ResponseEntity.ok(orderService.listAssets(customerId));
    }

/*
    @PostMapping("/admin/orders/{id}/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> matchOrder(@PathVariable Long id) {


        //Order order = orderRepo.findById(id).orElseThrow();

        /*
        // Update TRY and asset balances
        Asset tryAsset = assetRepo.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY");
        Asset stockAsset = assetRepo.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName());

        if (order.getSide() == OrderSide.BUY) {
            stockAsset.setSize(stockAsset.getSize().add(order.getSize()));
        } else {
            tryAsset.setSize(tryAsset.getSize().add(order.getSize().multiply(order.getPrice())));
        }

        order.setStatus(OrderStatus.MATCHED);


        //return ResponseEntity.ok(orderRepo.save(order));

        return ResponseEntity.noContent().build();
    }
*/

    @PostMapping("/admin/orders/{id}/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> matchOrder(@PathVariable Long id) {
        try {
            orderService.matchOrder(id);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        }

    }
}
