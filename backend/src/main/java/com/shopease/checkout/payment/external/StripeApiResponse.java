package com.shopease.checkout.payment.external;

/**
 * Simulates Stripe's API response shape.
 * Each provider returns data in a DIFFERENT format —
 * the Adapter pattern normalizes them all into PaymentResult.
 */
public record StripeApiResponse(
        String id,
        String status,       // "succeeded" | "failed"
        long amountCents,
        String currency,
        String receiptUrl
) {}
