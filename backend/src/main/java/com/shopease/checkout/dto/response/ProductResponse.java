package com.shopease.checkout.dto.response;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        double price,
        String imageUrl,
        String category
) {
}
