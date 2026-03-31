package com.shopease.checkout.dto.response;

import java.util.List;

public record ShippingQuoteResponse(
        String method,
        String label,
        double baseCost,
        double finalCost,
        List<String> appliedDiscounts
) {
}
