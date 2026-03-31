package com.shopease.checkout.dto.request;

import com.shopease.checkout.common.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CheckoutRequest(
        @NotEmpty(message = "Cart cannot be empty")
        List<CartItemDto> items,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotBlank(message = "Shipping method is required")
        String shippingMethod,

        @NotNull(message = "Currency is required")
        Currency currency
) {
}
