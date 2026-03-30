package com.shopease.checkout.payment;

public record PaymentResult(
        boolean success,
        String transactionId,
        String providerMessage,
        String provider
) {}
