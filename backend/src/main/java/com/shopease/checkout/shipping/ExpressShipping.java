package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public final class ExpressShipping implements ShippingStrategy {

    private static final double BASE_RATE = 14.99;
    private static final double PER_ITEM_RATE = 1.00;

    @Override public String getKey()   { return "EXPRESS"; }
    @Override public String getLabel() { return "Express Shipping (1-2 business days)"; }

    @Override
    public double calculateBaseCost(List<CartItemDto> items) {
        int totalQuantity = items.stream().mapToInt(CartItemDto::quantity).sum();
        return BASE_RATE + (totalQuantity * PER_ITEM_RATE);
    }
}
