package com.shopease.checkout.payment.external;

/**
 * Crypto provider uses yet another completely different format.
 */
public record CryptoApiResponse(
        String txHash,
        int confirmations,
        boolean confirmed,
        double amountInUsd,
        String walletAddress
) {}
