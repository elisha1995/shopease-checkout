package com.shopease.checkout.payment.adapter;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.payment.PaymentRequest;
import com.shopease.checkout.payment.PaymentResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StripePaymentAdapterTest {

    private final StripePaymentAdapter adapter = new StripePaymentAdapter();

    @Test
    void shouldProcessSuccessfulPayment() {
        var request = new PaymentRequest("ORD-123", 99.99, Currency.USD, "test@test.com");
        PaymentResult result = adapter.processPayment(request);

        assertTrue(result.success());
        assertNotNull(result.transactionId());
        assertTrue(result.transactionId().startsWith("ch_"));
        assertEquals("STRIPE", result.provider());
        assertTrue(result.providerMessage().contains("Receipt"));
    }

    @Test
    void shouldFailForZeroAmount() {
        var request = new PaymentRequest("ORD-123", 0, Currency.USD, "test@test.com");
        PaymentResult result = adapter.processPayment(request);
        assertFalse(result.success());
    }
}

class PayPalPaymentAdapterTest {

    private final PayPalPaymentAdapter adapter = new PayPalPaymentAdapter();

    @Test
    void shouldProcessSuccessfulPayment() {
        var request = new PaymentRequest("ORD-456", 55.00, Currency.EUR, "user@test.com");
        PaymentResult result = adapter.processPayment(request);

        assertTrue(result.success());
        assertTrue(result.transactionId().startsWith("PAY-"));
        assertEquals("PAYPAL", result.provider());
    }

    @Test
    void shouldIncludePayerEmail() {
        var request = new PaymentRequest("ORD-456", 55.00, Currency.USD, "user@test.com");
        PaymentResult result = adapter.processPayment(request);
        assertTrue(result.providerMessage().contains("user@test.com"));
    }

    @Test
    void shouldFailForZeroAmount() {
        var request = new PaymentRequest("ORD-456", 0, Currency.USD, "test@test.com");
        PaymentResult result = adapter.processPayment(request);
        assertFalse(result.success());
    }
}

class CryptoPaymentAdapterTest {

    private final CryptoPaymentAdapter adapter = new CryptoPaymentAdapter();

    @Test
    void shouldProcessSuccessfulPayment() {
        var request = new PaymentRequest("ORD-789", 120.00, Currency.USD, "crypto@test.com");
        PaymentResult result = adapter.processPayment(request);

        assertTrue(result.success());
        assertTrue(result.transactionId().startsWith("0x"));
        assertEquals("CRYPTO", result.provider());
        assertTrue(result.providerMessage().contains("confirmations"));
    }

    @Test
    void shouldFailForZeroAmount() {
        var request = new PaymentRequest("ORD-789", 0, Currency.USD, "test@test.com");
        PaymentResult result = adapter.processPayment(request);
        assertFalse(result.success());
    }
}
