package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MembershipDiscountRuleTest {

    private final GoldMemberDiscountRule goldRule = new GoldMemberDiscountRule();
    private final SilverMemberDiscountRule silverRule = new SilverMemberDiscountRule();
    private final PlatinumFreeShippingRule platinumRule = new PlatinumFreeShippingRule();
    private final List<CartItemDto> items = List.of(new CartItemDto("p1", "Item", 30.0, 1));

    @Test void goldShouldApplyOnlyToGoldMembers() {
        assertTrue(goldRule.applies(items, MembershipTier.GOLD));
        assertFalse(goldRule.applies(items, MembershipTier.STANDARD));
        assertFalse(goldRule.applies(items, MembershipTier.SILVER));
        assertFalse(goldRule.applies(items, MembershipTier.PLATINUM));
    }

    @Test void goldShouldApplyTwentyPercentDiscount() {
        assertEquals(8.0, goldRule.apply(10.0, items, MembershipTier.GOLD), 0.01);
    }

    @Test void silverShouldApplyOnlyToSilverMembers() {
        assertTrue(silverRule.applies(items, MembershipTier.SILVER));
        assertFalse(silverRule.applies(items, MembershipTier.GOLD));
    }

    @Test void silverShouldApplyTenPercentDiscount() {
        assertEquals(9.0, silverRule.apply(10.0, items, MembershipTier.SILVER), 0.01);
    }

    @Test void platinumShouldApplyOnlyToPlatinumMembers() {
        assertTrue(platinumRule.applies(items, MembershipTier.PLATINUM));
        assertFalse(platinumRule.applies(items, MembershipTier.GOLD));
    }

    @Test void platinumShouldReturnFreeShipping() {
        assertEquals(0.0, platinumRule.apply(15.99, items, MembershipTier.PLATINUM));
    }

    @Test void platinumShouldHaveHighestPriority() {
        assertTrue(platinumRule.priority() < goldRule.priority());
    }
}
