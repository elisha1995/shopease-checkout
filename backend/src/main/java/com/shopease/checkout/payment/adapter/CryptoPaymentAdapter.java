package com.shopease.checkout.payment.adapter;

import com.shopease.checkout.payment.PaymentProcessor;
import com.shopease.checkout.payment.PaymentRequest;
import com.shopease.checkout.payment.PaymentResult;
import com.shopease.checkout.payment.external.CryptoApiResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ADAPTER PATTERN: Crypto uses "txHash"/"confirmed" — completely different
 * from both Stripe and PayPal. The adapter normalizes it.
 */
@Component
public class CryptoPaymentAdapter implements PaymentProcessor {

    @Override
    public String getKey() {
        return "CRYPTO";
    }

    @Override
    public String getDisplayName() {
        return "Cryptocurrency (BTC/ETH)";
    }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        CryptoApiResponse cryptoResponse = simulateCryptoCall(request);

        // ADAPT: Crypto uses "confirmed" boolean and "txHash" for ID
        return new PaymentResult(
                cryptoResponse.confirmed(),
                cryptoResponse.txHash(),                       // Crypto uses "txHash"
                cryptoResponse.confirmed()
                        ? "Crypto payment confirmed with %d confirmations".formatted(cryptoResponse.confirmations())
                        : "Crypto payment awaiting confirmations",
                "CRYPTO"
        );
    }

    private CryptoApiResponse simulateCryptoCall(PaymentRequest request) {
        return new CryptoApiResponse(
                "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 40),
                request.amount() > 0 ? 6 : 0,
                request.amount() > 0,
                request.amount(),
                "0xWALLET" + UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
