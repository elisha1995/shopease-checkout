package com.shopease.checkout.payment.adapter;

import com.shopease.checkout.payment.PaymentProcessor;
import com.shopease.checkout.payment.PaymentRequest;
import com.shopease.checkout.payment.PaymentResult;
import com.shopease.checkout.payment.external.StripeApiResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ADAPTER PATTERN: Converts Stripe's unique API response shape
 * into our standardized PaymentResult.
 * <p>
 * In a real app, this would call the actual Stripe SDK.
 * Here we simulate the external call and demonstrate the pattern.
 */
@Component
public class StripePaymentAdapter implements PaymentProcessor {

    @Override
    public String getKey() {
        return "STRIPE";
    }

    @Override
    public String getDisplayName() {
        return "Stripe (Credit/Debit Card)";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // --- Simulate calling the Stripe API ---
        StripeApiResponse stripeResponse = simulateStripeCall(request);

        // --- ADAPT: Convert Stripe's shape → our standard PaymentResult ---
        boolean success = "succeeded".equals(stripeResponse.status());
        return new PaymentResult(
                success,
                stripeResponse.id(),                            // Stripe uses "id"
                success ? "Payment via Stripe successful. Receipt: " + stripeResponse.receiptUrl()
                        : "Stripe payment failed",
                "STRIPE"
        );
    }

    private StripeApiResponse simulateStripeCall(PaymentRequest request) {
        return new StripeApiResponse(
                "ch_" + UUID.randomUUID().toString().substring(0, 14),
                request.amount() > 0 ? "succeeded" : "failed",
                (long) (request.amount() * 100),
                request.currency().name().toLowerCase(),
                "https://receipt.stripe.com/" + UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
