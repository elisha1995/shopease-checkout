package com.shopease.checkout.shipping;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.dto.response.ShippingQuoteResponse;
import com.shopease.checkout.shipping.discount.ShippingDiscountChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShippingServiceImplTest {

    private ShippingServiceImpl shippingService;
    private ShippingDiscountChain discountChain;

    @BeforeEach
    void setUp() {
        discountChain = mock(ShippingDiscountChain.class);

        var standard = new StandardShipping();
        var express = new ExpressShipping();

        shippingService = new ShippingServiceImpl(List.of(standard, express), discountChain);
    }

    @Test
    void calculateReturnsQuoteForStandard() {
        var items = List.of(new CartItemDto("p1", "Earbuds", 29.99, 1));
        when(discountChain.applyDiscounts(anyDouble(), eq(items), eq(MembershipTier.STANDARD)))
                .thenReturn(new ShippingDiscountChain.DiscountResult(6.49, List.of()));

        var quote = shippingService.calculate("STANDARD", items, MembershipTier.STANDARD);

        assertEquals("STANDARD", quote.method());
        assertEquals(6.49, quote.baseCost());
        assertEquals(6.49, quote.finalCost());
        assertTrue(quote.appliedDiscounts().isEmpty());
    }

    @Test
    void calculateReturnsQuoteForExpress() {
        var items = List.of(new CartItemDto("p1", "Keyboard", 89.99, 1));
        when(discountChain.applyDiscounts(anyDouble(), eq(items), eq(MembershipTier.GOLD)))
                .thenReturn(new ShippingDiscountChain.DiscountResult(0.0, List.of("Free shipping over $50")));

        var quote = shippingService.calculate("EXPRESS", items, MembershipTier.GOLD);

        assertEquals("EXPRESS", quote.method());
        assertEquals(0.0, quote.finalCost());
        assertEquals(1, quote.appliedDiscounts().size());
    }

    @Test
    void calculateIsCaseInsensitive() {
        var items = List.of(new CartItemDto("p1", "Hub", 34.50, 1));
        when(discountChain.applyDiscounts(anyDouble(), any(), any()))
                .thenReturn(new ShippingDiscountChain.DiscountResult(6.49, List.of()));

        var quote = shippingService.calculate("standard", items, MembershipTier.STANDARD);
        assertEquals("STANDARD", quote.method());
    }

    @Test
    void calculateThrowsForUnknownMethod() {
        var items = List.of(new CartItemDto("p1", "Earbuds", 29.99, 1));
        assertThrows(IllegalArgumentException.class,
                () -> shippingService.calculate("OVERNIGHT", items, MembershipTier.STANDARD));
    }

    @Test
    void calculateAllReturnsAllMethods() {
        var items = List.of(new CartItemDto("p1", "Earbuds", 29.99, 1));
        when(discountChain.applyDiscounts(anyDouble(), any(), any()))
                .thenReturn(new ShippingDiscountChain.DiscountResult(5.0, List.of()));

        var quotes = shippingService.calculateAll(items, MembershipTier.STANDARD);

        assertEquals(2, quotes.size());
        var methods = quotes.stream().map(ShippingQuoteResponse::method).toList();
        assertTrue(methods.contains("STANDARD"));
        assertTrue(methods.contains("EXPRESS"));
    }
}
