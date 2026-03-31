package com.shopease.checkout.dto.response;

public record OrderItemResponse(
        String productName,
        double price,
        int quantity
) {
}
