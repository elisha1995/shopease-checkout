package com.shopease.checkout.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ShippingCalculateRequest(
        String method,

        @NotEmpty(message = "Items cannot be empty")
        List<CartItemDto> items
) {
}
