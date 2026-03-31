package com.shopease.checkout.notification.service;

import com.shopease.checkout.notification.NotificationPayload;
import com.shopease.checkout.common.model.NotificationChannel;

import java.util.List;
import java.util.UUID;

/**
 * ISP (Interface Segregation): Only exposes what consumers need.
 * Hides retry logic, persistence, and channel routing internally.
 */
public interface NotificationService {

    void sendWithRetryAndPersist(UUID orderId, List<NotificationChannel> channels, NotificationPayload payload);
}
