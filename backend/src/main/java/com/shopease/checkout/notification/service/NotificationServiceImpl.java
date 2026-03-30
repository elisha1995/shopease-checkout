package com.shopease.checkout.notification.service;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.dto.response.NotificationLogResponse;
import com.shopease.checkout.entity.NotificationLogEntity;
import com.shopease.checkout.mapper.OrderMapper;
import com.shopease.checkout.notification.*;
import com.shopease.checkout.repository.NotificationLogRepository;
import com.shopease.checkout.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final RetryableNotificationSender retryableSender;
    private final NotificationLogRepository logRepository;
    private final OrderRepository orderRepository;

    public NotificationServiceImpl(RetryableNotificationSender retryableSender,
                                   NotificationLogRepository logRepository,
                                   OrderRepository orderRepository) {
        this.retryableSender = retryableSender;
        this.logRepository = logRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void sendWithRetryAndPersist(String orderId, List<NotificationChannel> channels, NotificationPayload payload) {
        var order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Cannot persist notifications — order {} not found", orderId);
            return;
        }

        // Default to EMAIL if no preferences set
        var effectiveChannels = (channels == null || channels.isEmpty())
                ? List.of(NotificationChannel.EMAIL)
                : channels;

        var results = new ArrayList<NotificationResult>();
        for (var channel : effectiveChannels) {
            var result = retryableSender.sendWithRetry(channel, payload);
            results.add(result);
            log.info("Notification via {}: success={}, attempts={}", channel, result.success(), result.attempts());
        }

        // Persist all results
        for (var result : results) {
            var entity = new NotificationLogEntity();
            entity.setOrder(order);
            entity.setChannel(result.channel());
            entity.setSuccess(result.success());
            entity.setMessage(result.message());
            entity.setAttempts(result.attempts());
            logRepository.save(entity);
        }
    }

    @Override
    public List<NotificationLogResponse> getNotificationsForOrder(String orderId) {
        return logRepository.findByOrderId(orderId).stream()
                .map(OrderMapper::toNotificationResponse)
                .toList();
    }
}
