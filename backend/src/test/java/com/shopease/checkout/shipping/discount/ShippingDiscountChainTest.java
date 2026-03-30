package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShippingDiscountChainTest {

    private final ShippingDiscountChain chain = new ShippingDiscountChain(List.of(
            new PlatinumFreeShippingRule(),
            new FreeShippingOverThresholdRule(),
            new GoldMemberDiscountRule(),
            new SilverMemberDiscountRule()
    ));

    @Test void standardUserUnderThreshold_noDiscount() {
        var items = List.of(new CartItemDto("p1", "Item", 30.0, 1));
        var result = chain.applyDiscounts(8.49, items, MembershipTier.STANDARD);
        assertEquals(8.49, result.finalCost());
        assertTrue(result.appliedDiscounts().isEmpty());
    }

    @Test void standardUserOverThreshold_freeShipping() {
        var items = List.of(new CartItemDto("p1", "Item", 60.0, 1));
        var result = chain.applyDiscounts(8.49, items, MembershipTier.STANDARD);
        assertEquals(0.0, result.finalCost());
    }

    @Test void goldUserUnderThreshold_twentyPercentOff() {
        var items = List.of(new CartItemDto("p1", "Item", 30.0, 1));
        var result = chain.applyDiscounts(10.0, items, MembershipTier.GOLD);
        assertEquals(8.0, result.finalCost());
    }

    @Test void goldUserOverThreshold_freeShippingTakesPriority() {
        var items = List.of(new CartItemDto("p1", "Item", 60.0, 1));
        var result = chain.applyDiscounts(10.0, items, MembershipTier.GOLD);
        assertEquals(0.0, result.finalCost());
    }

    @Test void silverUserUnderThreshold_tenPercentOff() {
        var items = List.of(new CartItemDto("p1", "Item", 30.0, 1));
        var result = chain.applyDiscounts(10.0, items, MembershipTier.SILVER);
        assertEquals(9.0, result.finalCost());
    }

    @Test void platinumUser_alwaysFreeRegardlessOfTotal() {
        var items = List.of(new CartItemDto("p1", "Item", 10.0, 1));
        var result = chain.applyDiscounts(15.99, items, MembershipTier.PLATINUM);
        assertEquals(0.0, result.finalCost());
    }

    @Test void addingNewRuleDoesNotBreakExisting() {
        var items = List.of(new CartItemDto("p1", "Item", 30.0, 1));
        assertEquals(10.0, chain.applyDiscounts(10.0, items, MembershipTier.STANDARD).finalCost());
        assertEquals(8.0, chain.applyDiscounts(10.0, items, MembershipTier.GOLD).finalCost());
    }
}
