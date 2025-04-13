package org.example.hubs_brokerage.dto;

import org.example.hubs_brokerage.model.OrderSide;

public record OrderRequest(
        String customerId,
        String assetName,
        OrderSide side,
        double size,
        double price
) {}
