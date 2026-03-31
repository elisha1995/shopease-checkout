package com.shopease.checkout.mapper;

import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.dto.response.NotificationLogResponse;
import com.shopease.checkout.dto.response.OrderItemResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.entity.NotificationLogEntity;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderItemEntity toItemEntity(CartItemDto dto) {
        var entity = new OrderItemEntity();
        entity.setProductName(dto.productName());
        entity.setPrice(BigDecimal.valueOf(dto.price()));
        entity.setQuantity(dto.quantity());
        return entity;
    }

    public static OrderResponse toResponse(OrderEntity entity, List<NotificationLogEntity> notifications) {
        var items = entity.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        var notifs = notifications.stream()
                .map(OrderMapper::toNotificationResponse)
                .toList();

        return new OrderResponse(
                entity.getOrderNumber(),
                entity.getUser().getId().toString(),
                items,
                entity.getSubtotal().doubleValue(),
                entity.getShippingCost().doubleValue(),
                entity.getTotal().doubleValue(),
                entity.getCurrency().name(),
                entity.getPaymentMethod(),
                entity.getTransactionId(),
                entity.getShippingMethod(),
                entity.getStatus(),
                entity.getCreatedAt().toString(),
                notifs
        );
    }

    public static OrderItemResponse toItemResponse(OrderItemEntity entity) {
        return new OrderItemResponse(
                entity.getProductName(),
                entity.getPrice().doubleValue(),
                entity.getQuantity()
        );
    }

    public static NotificationLogResponse toNotificationResponse(NotificationLogEntity entity) {
        return new NotificationLogResponse(
                entity.getChannel().name(),
                entity.isSuccess(),
                entity.getMessage(),
                entity.getAttempts()
        );
    }
}
