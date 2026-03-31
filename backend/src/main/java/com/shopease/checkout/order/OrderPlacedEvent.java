package com.shopease.checkout.order;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.common.model.NotificationChannel;

import java.util.List;
import java.util.UUID;

/**
 * OBSERVER PATTERN: This event is published when an order is placed.
 * Any listener can react to it — the publisher doesn't know who's listening.
 */
public record OrderPlacedEvent(
        UUID orderId,
        String orderNumber,
        String userId,
        String userName,
        String userEmail,
        String userPhone,
        List<NotificationChannel> notificationChannels,
        double total,
        Currency currency,
        String paymentMethod,
        String transactionId
) {
}
