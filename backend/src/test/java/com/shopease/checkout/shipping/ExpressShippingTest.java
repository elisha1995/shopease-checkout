package com.shopease.checkout.shipping;

import com.shopease.checkout.dto.request.CartItemDto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExpressShippingTest {

    private final ExpressShipping strategy = new ExpressShipping();

    @Test void shouldReturnExpressKey() { assertEquals("EXPRESS", strategy.getKey()); }

    @Test void shouldCalculateHigherBaseCost() {
        var items = List.of(new CartItemDto("p1", "Widget", 10.0, 1));
        assertEquals(15.99, strategy.calculateBaseCost(items), 0.01);
    }

    @Test void shouldScaleCostWithQuantity() {
        var items = List.of(
                new CartItemDto("p1", "Widget", 10.0, 2),
                new CartItemDto("p2", "Gadget", 20.0, 3));
        assertEquals(19.99, strategy.calculateBaseCost(items), 0.01);
    }
}
