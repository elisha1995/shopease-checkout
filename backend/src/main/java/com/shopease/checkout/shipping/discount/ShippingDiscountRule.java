package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import java.util.List;

/**
 * OPEN-CLOSED PRINCIPLE: New discount rules implement this interface
 * and are auto-discovered via Spring component scanning.
 * 
 * Accepts only DTOs and enums — no entity coupling in business logic.
 */
public interface ShippingDiscountRule {

    int priority();
    boolean applies(List<CartItemDto> items, MembershipTier tier);
    double apply(double currentCost, List<CartItemDto> items, MembershipTier tier);
    String description();
}
