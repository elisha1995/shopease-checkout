package com.shopease.checkout.dto.response;

import java.util.List;

public record OrderResponse(
        String id,
        String userId,
        List<OrderItemResponse> items,
        double subtotal,
        double shippingCost,
        double total,
        String currency,
        String paymentMethod,
        String transactionId,
        String shippingMethod,
        String status,
        String createdAt,
        List<NotificationLogResponse> notifications
) {}
