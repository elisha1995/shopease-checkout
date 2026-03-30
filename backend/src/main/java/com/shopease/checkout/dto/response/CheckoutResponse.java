package com.shopease.checkout.dto.response;

public record CheckoutResponse(
        boolean success,
        String orderId,
        String message
) {}
