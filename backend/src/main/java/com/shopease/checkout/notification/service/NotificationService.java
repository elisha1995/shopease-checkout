package com.shopease.checkout.notification.service;

import com.shopease.checkout.dto.response.NotificationLogResponse;
import com.shopease.checkout.notification.NotificationPayload;
import com.shopease.checkout.common.model.NotificationChannel;

import java.util.List;

/**
 * ISP (Interface Segregation): Only exposes what consumers need.
 * Hides retry logic, persistence, and channel routing internally.
 */
public interface NotificationService {

    void sendWithRetryAndPersist(String orderId, List<NotificationChannel> channels, NotificationPayload payload);

    List<NotificationLogResponse> getNotificationsForOrder(String orderId);
}
