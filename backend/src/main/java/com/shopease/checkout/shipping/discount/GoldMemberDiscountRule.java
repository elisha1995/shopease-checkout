package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class GoldMemberDiscountRule implements ShippingDiscountRule {

    @Override public int priority() { return 50; }

    @Override
    public boolean applies(List<CartItemDto> items, MembershipTier tier) {
        return tier == MembershipTier.GOLD;
    }

    @Override
    public double apply(double currentCost, List<CartItemDto> items, MembershipTier tier) {
        return currentCost * 0.80;
    }

    @Override
    public String description() { return "Gold members get 20% off shipping"; }
}
