package com.shopease.checkout.order;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.common.model.NotificationChannel;

import java.util.List;

/**
 * OBSERVER PATTERN: This event is published when an order is placed.
 * Any listener can react to it — the publisher doesn't know who's listening.
 */
public record OrderPlacedEvent(
        String orderId,
        String userId,
        String userName,
        String userEmail,
        String userPhone,
        List<NotificationChannel> notificationChannels,
        double total,
        Currency currency,
        String paymentMethod,
        String transactionId
) {}
