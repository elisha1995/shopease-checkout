package com.shopease.checkout.payment;

import com.shopease.checkout.common.model.Currency;

public record PaymentRequest(
        String orderId,
        double amount,
        Currency currency,
        String customerEmail
) {}
