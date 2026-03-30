package com.shopease.checkout.payment.external;

/**
 * PayPal uses different field names and nesting than Stripe.
 */
public record PayPalApiResponse(
        String paymentId,
        String state,         // "approved" | "failed"
        double totalAmount,
        String currencyCode,
        PayerInfo payer
) {
    public record PayerInfo(String email, String payerId) {}
}
