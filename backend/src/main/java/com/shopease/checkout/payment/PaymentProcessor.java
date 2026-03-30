package com.shopease.checkout.payment;

/**
 * STRATEGY PATTERN: All payment providers implement this interface.
 * The checkout flow depends on this interface, never on concrete providers.
 */
public interface PaymentProcessor {

    /** Unique key: "STRIPE", "PAYPAL", "CRYPTO" */
    String getKey();

    String getDisplayName();

    /** Process a payment and return a standardized result */
    PaymentResult processPayment(PaymentRequest request);
}
