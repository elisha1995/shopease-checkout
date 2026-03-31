package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Composes all ShippingDiscountRule beans discovered by Spring.
 * Rules are sorted by priority and applied in sequence.
 */
@Component
public class ShippingDiscountChain {

    private final List<ShippingDiscountRule> rules;

    public ShippingDiscountChain(List<ShippingDiscountRule> rules) {
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(ShippingDiscountRule::priority))
                .toList();
    }

    public record DiscountResult(double finalCost, List<String> appliedDiscounts) {
    }

    public DiscountResult applyDiscounts(double baseCost, List<CartItemDto> items, MembershipTier tier) {
        double cost = baseCost;
        var applied = new ArrayList<String>();

        for (var rule : rules) {
            if (rule.applies(items, tier)) {
                cost = rule.apply(cost, items, tier);
                applied.add(rule.description());
                if (cost <= 0.0) {
                    cost = 0.0;
                    break;
                }
            }
        }

        return new DiscountResult(Math.round(cost * 100.0) / 100.0, applied);
    }
}
