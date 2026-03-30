package com.shopease.checkout.payment.adapter;

import com.shopease.checkout.payment.PaymentProcessor;
import com.shopease.checkout.payment.PaymentRequest;
import com.shopease.checkout.payment.PaymentResult;
import com.shopease.checkout.payment.external.PayPalApiResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ADAPTER PATTERN: PayPal uses "state"/"approved" where Stripe uses "status"/"succeeded".
 * The adapter hides these differences from the checkout logic.
 */
@Component
public class PayPalPaymentAdapter implements PaymentProcessor {

    @Override
    public String getKey() {
        return "PAYPAL";
    }

    @Override
    public String getDisplayName() {
        return "PayPal";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        PayPalApiResponse paypalResponse = simulatePayPalCall(request);

        // ADAPT: PayPal uses "state"/"approved", map to our boolean success
        boolean success = "approved".equals(paypalResponse.state());
        return new PaymentResult(
                success,
                paypalResponse.paymentId(),                    // PayPal uses "paymentId"
                success ? "PayPal payment approved for payer: " + paypalResponse.payer().email()
                        : "PayPal payment not approved",
                "PAYPAL"
        );
    }

    private PayPalApiResponse simulatePayPalCall(PaymentRequest request) {
        return new PayPalApiResponse(
                "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(),
                request.amount() > 0 ? "approved" : "failed",
                request.amount(),
                request.currency().name(),
                new PayPalApiResponse.PayerInfo(request.customerEmail(), "PAYER-" + UUID.randomUUID().toString().substring(0, 6))
        );
    }
}
