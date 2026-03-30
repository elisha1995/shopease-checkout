package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

/** EXTRA WHAT-IF: Added without modifying any existing rule. */
@Component
public class PlatinumFreeShippingRule implements ShippingDiscountRule {

    @Override public int priority() { return 5; }

    @Override
    public boolean applies(List<CartItemDto> items, MembershipTier tier) {
        return tier == MembershipTier.PLATINUM;
    }

    @Override
    public double apply(double currentCost, List<CartItemDto> items, MembershipTier tier) {
        return 0.0;
    }

    @Override
    public String description() { return "Platinum members enjoy free shipping"; }
}
