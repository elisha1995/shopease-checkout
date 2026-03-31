package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoldMemberDiscountRule implements ShippingDiscountRule {

    private final double discountMultiplier;
    private final int discountPercent;

    public GoldMemberDiscountRule(@Value("${shipping.gold-discount-percent}") int discountPercent) {
        this.discountPercent = discountPercent;
        this.discountMultiplier = 1.0 - (discountPercent / 100.0);
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public boolean applies(List<CartItemDto> items, MembershipTier tier) {
        return tier == MembershipTier.GOLD;
    }

    @Override
    public double apply(double currentCost, List<CartItemDto> items, MembershipTier tier) {
        return currentCost * discountMultiplier;
    }

    @Override
    public String description() {
        return "Gold members get %d%% off shipping".formatted(discountPercent);
    }
}