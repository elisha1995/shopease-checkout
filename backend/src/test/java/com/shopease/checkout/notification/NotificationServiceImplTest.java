package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.notification.service.NotificationServiceImpl;
import com.shopease.checkout.repository.NotificationLogRepository;
import com.shopease.checkout.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private RetryableNotificationSender retryableSender;
    private NotificationLogRepository logRepository;
    private OrderRepository orderRepository;
    private NotificationServiceImpl service;

    private final UUID orderId = UUID.randomUUID();
    private final NotificationPayload payload = new NotificationPayload(
            "u1", "test@test.com", "+233200000000", "Subject",
            "Body", null, "ORD-001"
    );

    @BeforeEach
    void setUp() {
        retryableSender = mock(RetryableNotificationSender.class);
        logRepository = mock(NotificationLogRepository.class);
        orderRepository = mock(OrderRepository.class);
        service = new NotificationServiceImpl(retryableSender, logRepository, orderRepository);
    }

    @Test
    void sendsNotificationForEachChannelAndPersists() {
        var order = new OrderEntity();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(retryableSender.sendWithRetry(any(), eq(payload)))
                .thenReturn(new NotificationResult(NotificationChannel.EMAIL, true,
                        "Sent", 1));

        service.sendWithRetryAndPersist(orderId, List.of(NotificationChannel.EMAIL), payload);

        verify(retryableSender).sendWithRetry(NotificationChannel.EMAIL, payload);
        verify(logRepository).save(any());
    }

    @Test
    void sendsToMultipleChannels() {
        var order = new OrderEntity();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(retryableSender.sendWithRetry(NotificationChannel.EMAIL, payload))
                .thenReturn(new NotificationResult(NotificationChannel.EMAIL, true,
                        "Email sent", 1));
        when(retryableSender.sendWithRetry(NotificationChannel.SMS, payload))
                .thenReturn(new NotificationResult(NotificationChannel.SMS, true,
                        "SMS sent", 1));

        service.sendWithRetryAndPersist(orderId, List.of(NotificationChannel.EMAIL,
                NotificationChannel.SMS), payload);

        verify(retryableSender).sendWithRetry(NotificationChannel.EMAIL, payload);
        verify(retryableSender).sendWithRetry(NotificationChannel.SMS, payload);
        verify(logRepository, times(2)).save(any());
    }

    @Test
    void defaultsToEmailWhenChannelsNull() {
        var order = new OrderEntity();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(retryableSender.sendWithRetry(any(), any()))
                .thenReturn(new NotificationResult(NotificationChannel.EMAIL, true,
                        "Sent", 1));

        service.sendWithRetryAndPersist(orderId, null, payload);

        verify(retryableSender).sendWithRetry(NotificationChannel.EMAIL, payload);
    }

    @Test
    void defaultsToEmailWhenChannelsEmpty() {
        var order = new OrderEntity();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(retryableSender.sendWithRetry(any(), any()))
                .thenReturn(new NotificationResult(NotificationChannel.EMAIL, true,
                        "Sent", 1));

        service.sendWithRetryAndPersist(orderId, List.of(), payload);

        verify(retryableSender).sendWithRetry(NotificationChannel.EMAIL, payload);
    }

    @Test
    void skipsWhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        service.sendWithRetryAndPersist(orderId, List.of(NotificationChannel.EMAIL), payload);

        verify(retryableSender, never()).sendWithRetry(any(), any());
        verify(logRepository, never()).save(any());
    }
}
