package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.payment.adapter.CryptoPaymentAdapter;
import com.shopease.checkout.payment.adapter.PayPalPaymentAdapter;
import com.shopease.checkout.payment.adapter.StripePaymentAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentProcessorFactoryTest {

    private final PaymentProcessorFactory factory = new PaymentProcessorFactory(List.of(
            new StripePaymentAdapter(),
            new PayPalPaymentAdapter(),
            new CryptoPaymentAdapter()
    ));

    @Test
    void shouldReturnStripeProcessor() {
        PaymentProcessor processor = factory.create("STRIPE");
        assertNotNull(processor);
        assertEquals("STRIPE", processor.getKey());
    }

    @Test
    void shouldReturnPayPalProcessor() {
        PaymentProcessor processor = factory.create("PAYPAL");
        assertNotNull(processor);
        assertEquals("PAYPAL", processor.getKey());
    }

    @Test
    void shouldReturnCryptoProcessor() {
        PaymentProcessor processor = factory.create("CRYPTO");
        assertNotNull(processor);
        assertEquals("CRYPTO", processor.getKey());
    }

    @Test
    void shouldBeCaseInsensitive() {
        assertNotNull(factory.create("stripe"));
        assertNotNull(factory.create("Paypal"));
    }

    @Test
    void shouldThrowOnUnknownMethod() {
        var ex = assertThrows(IllegalArgumentException.class, () -> factory.create("BITCOIN"));
        assertTrue(ex.getMessage().contains("Unknown payment method"));
        assertTrue(ex.getMessage().contains("BITCOIN"));
    }

    @Test
    void shouldListAvailableMethods() {
        var methods = factory.availableMethods();
        assertEquals(3, methods.size());
    }
}
