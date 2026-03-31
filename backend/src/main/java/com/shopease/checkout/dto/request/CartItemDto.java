package com.shopease.checkout.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CartItemDto(
        String productId,

        @NotBlank(message = "Product name is required")
        String productName,

        @Positive(message = "Price must be positive")
        double price,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
    public double subtotal() {
        return price * quantity;
    }
}
