package com.shopease.checkout.shipping.discount;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MembershipDiscountRuleTest {

    private final GoldMemberDiscountRule goldRule = new GoldMemberDiscountRule(20);
    private final List<CartItemDto> items = List.of(new CartItemDto("p1", "Item", 30.0, 1));

    @Test void goldShouldApplyOnlyToGoldMembers() {
        assertTrue(goldRule.applies(items, MembershipTier.GOLD));
        assertFalse(goldRule.applies(items, MembershipTier.STANDARD));
    }

    @Test void goldShouldApplyTwentyPercentDiscount() {
        assertEquals(8.0, goldRule.apply(10.0, items, MembershipTier.GOLD), 0.01);
    }
}
