package com.shopease.checkout.dto.response;

public record CurrencyResponse(
        String code,
        double rateToUsd
) {
}
