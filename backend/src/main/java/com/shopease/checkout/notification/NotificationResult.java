package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;

public record NotificationResult(
        NotificationChannel channel,
        boolean success,
        String message,
        int attempts
) {
}
