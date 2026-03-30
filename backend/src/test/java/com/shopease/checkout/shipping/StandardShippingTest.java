package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StandardShippingTest {

    private final StandardShipping strategy = new StandardShipping();

    @Test void shouldReturnStandardKey() { assertEquals("STANDARD", strategy.getKey()); }

    @Test void shouldCalculateBaseCostForSingleItem() {
        var items = List.of(new CartItemDto("p1", "Widget", 10.0, 1));
        assertEquals(6.49, strategy.calculateBaseCost(items), 0.01);
    }

    @Test void shouldScaleCostWithQuantity() {
        var items = List.of(
                new CartItemDto("p1", "Widget", 10.0, 3),
                new CartItemDto("p2", "Gadget", 20.0, 2));
        assertEquals(8.49, strategy.calculateBaseCost(items), 0.01);
    }

    @Test void shouldHandleEmptyCart() {
        assertEquals(5.99, strategy.calculateBaseCost(List.of()), 0.01);
    }
}
