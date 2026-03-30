package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class FreeShippingOverThresholdRule implements ShippingDiscountRule {

    private static final double THRESHOLD = 50.0;

    @Override public int priority() { return 10; }

    @Override
    public boolean applies(List<CartItemDto> items, MembershipTier tier) {
        return items.stream().mapToDouble(CartItemDto::subtotal).sum() > THRESHOLD;
    }

    @Override
    public double apply(double currentCost, List<CartItemDto> items, MembershipTier tier) {
        return 0.0;
    }

    @Override
    public String description() { return "Free shipping on orders over $%.0f".formatted(THRESHOLD); }
}
