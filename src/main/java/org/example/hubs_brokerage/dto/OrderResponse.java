package org.example.hubs_brokerage.dto;

import org.example.hubs_brokerage.model.Order;
import org.example.hubs_brokerage.model.OrderSide;
import org.example.hubs_brokerage.model.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String customerId,
        String assetName,
        OrderSide side,
        double size,
        double price,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getAssetName(),
                order.getSide(),
                order.getSize(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
