package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;

/**
 * STRATEGY PATTERN: Each notification channel (Email, SMS, Push, Slack)
 * implements this interface. Adding a new channel = one new class.
 */
public interface NotificationSender {

    NotificationChannel getChannel();

    NotificationResult send(NotificationPayload payload);
}
