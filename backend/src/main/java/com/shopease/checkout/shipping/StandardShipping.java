package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public final class StandardShipping implements ShippingStrategy {

    private static final double BASE_RATE = 5.99;
    private static final double PER_ITEM_RATE = 0.50;

    @Override public String getKey()   { return "STANDARD"; }
    @Override public String getLabel() { return "Standard Shipping (5-7 business days)"; }

    @Override
    public double calculateBaseCost(List<CartItemDto> items) {
        int totalQuantity = items.stream().mapToInt(CartItemDto::quantity).sum();
        return BASE_RATE + (totalQuantity * PER_ITEM_RATE);
    }
}
