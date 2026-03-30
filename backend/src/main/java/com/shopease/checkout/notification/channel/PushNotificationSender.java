package com.shopease.checkout.notification.channel;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.NotificationPayload;
import com.shopease.checkout.notification.NotificationResult;
import com.shopease.checkout.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Push notification sender stub.
 * In production, this would integrate with Firebase Cloud Messaging (FCM)
 * or Apple Push Notification Service (APNs). For this demo, it logs the
 * notification and returns success, simulating a successful push delivery.
 */
@Component
public class PushNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationSender.class);

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        // In production: call FCM API with device token from user profile
        log.info("[PUSH] Simulated push to user {} | Title: {} | Body: {}",
                payload.recipientId(), payload.subject(), payload.body());

        return new NotificationResult(
                NotificationChannel.PUSH, true,
                "Push notification sent to user " + payload.recipientId() + " (simulated)", 1
        );
    }
}
