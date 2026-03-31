package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FreeShippingOverThresholdRuleTest {

    private final FreeShippingOverThresholdRule rule = new FreeShippingOverThresholdRule(50.0);

    @Test
    void shouldApplyWhenCartIsOverFifty() {
        var items = List.of(new CartItemDto("p1", "Stand", 55.0, 1));
        assertTrue(rule.applies(items, MembershipTier.STANDARD));
    }

    @Test
    void shouldNotApplyWhenCartIsExactlyFifty() {
        var items = List.of(new CartItemDto("p1", "Item", 50.0, 1));
        assertFalse(rule.applies(items, MembershipTier.STANDARD));
    }

    @Test
    void shouldNotApplyWhenCartIsUnderFifty() {
        var items = List.of(new CartItemDto("p1", "Item", 49.99, 1));
        assertFalse(rule.applies(items, MembershipTier.STANDARD));
    }

    @Test
    void shouldReturnZeroCost() {
        var items = List.of(new CartItemDto("p1", "Item", 60.0, 1));
        assertEquals(0.0, rule.apply(8.99, items, MembershipTier.STANDARD));
    }

    @Test
    void shouldApplyWithMultipleItemsOverThreshold() {
        var items = List.of(
                new CartItemDto("p1", "A", 30.0, 1),
                new CartItemDto("p2", "B", 25.0, 1));
        assertTrue(rule.applies(items, MembershipTier.STANDARD));
    }
}
