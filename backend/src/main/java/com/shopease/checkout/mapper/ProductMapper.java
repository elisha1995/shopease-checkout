package com.shopease.checkout.mapper;

import com.shopease.checkout.dto.response.ProductResponse;
import com.shopease.checkout.entity.ProductEntity;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId().toString(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice().doubleValue(),
                entity.getImageUrl(),
                entity.getCategory()
        );
    }
}
