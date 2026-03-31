package com.shopease.checkout.notification;

public record NotificationPayload(
        String recipientId,
        String recipientEmail,
        String recipientPhone,
        String subject,
        String body,
        String orderId
) {
}
