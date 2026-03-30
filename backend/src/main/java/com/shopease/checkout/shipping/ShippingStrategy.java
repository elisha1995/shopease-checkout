package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.CartItemDto;
import java.util.List;

/**
 * STRATEGY PATTERN: Each shipping method implements this sealed interface.
 * Sealed to make exhaustive switch expressions possible (Java 21+).
 */
public sealed interface ShippingStrategy permits StandardShipping, ExpressShipping {

    String getKey();
    String getLabel();
    double calculateBaseCost(List<CartItemDto> items);
}
