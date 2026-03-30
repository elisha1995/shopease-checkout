package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

/** EXTRA WHAT-IF: Added without modifying any existing rule. */
@Component
public class SilverMemberDiscountRule implements ShippingDiscountRule {

    @Override public int priority() { return 50; }

    @Override
    public boolean applies(List<CartItemDto> items, MembershipTier tier) {
        return tier == MembershipTier.SILVER;
    }

    @Override
    public double apply(double currentCost, List<CartItemDto> items, MembershipTier tier) {
        return currentCost * 0.90;
    }

    @Override
    public String description() { return "Silver members get 10% off shipping"; }
}
