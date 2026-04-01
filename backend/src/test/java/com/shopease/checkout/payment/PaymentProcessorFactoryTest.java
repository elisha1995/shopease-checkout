package com.shopease.checkout.payment;

import com.shopease.checkout.payment.adapter.CryptoPaymentAdapter;
import com.shopease.checkout.payment.adapter.PayPalPaymentAdapter;
import com.shopease.checkout.payment.adapter.StripePaymentAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentProcessorFactoryTest {

    private final PaymentProcessorFactory factory = new PaymentProcessorFactory(List.of(
            new StripePaymentAdapter(),
            new PayPalPaymentAdapter(),
            new CryptoPaymentAdapter()
    ));

    @ParameterizedTest
    @ValueSource(strings = {"STRIPE", "PAYPAL", "CRYPTO"})
    void shouldReturnProcessorForKey(String key) {
        PaymentProcessor processor = factory.create(key);
        assertNotNull(processor);
        assertEquals(key, processor.getKey());
    }

    @Test
    void shouldBeCaseInsensitive() {
        assertNotNull(factory.create("stripe"));
        assertNotNull(factory.create("Paypal"));
    }

    @Test
    void shouldThrowOnUnknownMethod() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> factory.create("BITCOIN"));
        assertTrue(ex.getMessage().contains("Unknown payment method"));
        assertTrue(ex.getMessage().contains("BITCOIN"));
    }

    @Test
    void shouldListAvailableMethods() {
        var methods = factory.availableMethods();
        assertEquals(3, methods.size());
    }
}
