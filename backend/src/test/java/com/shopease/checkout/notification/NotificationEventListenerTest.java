package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.service.NotificationService;
import com.shopease.checkout.order.OrderPlacedEvent;
import com.shopease.checkout.security.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationEventListenerTest {

    private NotificationService notificationService;
    private NotificationSenderFactory senderFactory;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        senderFactory = mock(NotificationSenderFactory.class);
        listener = new NotificationEventListener(notificationService, senderFactory);
    }

    @Test
    void onOrderPlacedSendsNotificationWithCorrectPayload() {
        var orderId = UUID.randomUUID();
        var channels = List.of(NotificationChannel.EMAIL, NotificationChannel.SMS);
        var event = new OrderPlacedEvent(
                orderId, "ORD-20260401-ABCD",
                "user-1", "Kwame", "kwame@test.com", "+233240000000",
                channels, 66.47, Currency.USD, "STRIPE", "ch_abc123"
        );

        listener.onOrderPlaced(event);

        var payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(notificationService).sendWithRetryAndPersist(
                eq(orderId), eq(channels), payloadCaptor.capture()
        );

        var payload = payloadCaptor.getValue();
        assertEquals("kwame@test.com", payload.recipientEmail());
        assertEquals("+233240000000", payload.recipientPhone());
        assertTrue(payload.subject().contains("ORD-20260401-ABCD"));
        assertTrue(payload.body().contains("Kwame"));
        assertTrue(payload.body().contains("66.47"));
        assertNotNull(payload.htmlBody());
        assertTrue(payload.htmlBody().contains("Order Confirmed"));
    }

    @Test
    void onOrderPlacedIncludesPaymentDetailsInBody() {
        var event = new OrderPlacedEvent(
                UUID.randomUUID(), "ORD-001",
                "u1", "Ama", "ama@test.com", null,
                List.of(NotificationChannel.EMAIL),
                100.00, Currency.GHS, "PAYPAL", "PAY-xyz"
        );

        listener.onOrderPlaced(event);

        var payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(notificationService).sendWithRetryAndPersist(any(), any(), payloadCaptor.capture());

        var payload = payloadCaptor.getValue();
        assertTrue(payload.body().contains("PAYPAL"));
        assertTrue(payload.body().contains("PAY-xyz"));
        assertTrue(payload.body().contains("GHS"));
    }

    @Test
    void onUserRegisteredSendsWelcomeEmail() {
        var event = new UserRegisteredEvent("Kofi", "kofi@test.com");

        var emailSender = mock(NotificationSender.class);
        when(senderFactory.create(NotificationChannel.EMAIL)).thenReturn(emailSender);
        when(emailSender.send(any())).thenReturn(
                new NotificationResult(NotificationChannel.EMAIL, true, "Sent", 1)
        );

        listener.onUserRegistered(event);

        var payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(emailSender).send(payloadCaptor.capture());

        var payload = payloadCaptor.getValue();
        assertEquals("kofi@test.com", payload.recipientEmail());
        assertEquals("Welcome to ShopEase!", payload.subject());
        assertTrue(payload.body().contains("Kofi"));
        assertNotNull(payload.htmlBody());
        assertTrue(payload.htmlBody().contains("Welcome to ShopEase"));
    }

    @Test
    void onUserRegisteredAlwaysUsesEmailChannel() {
        var event = new UserRegisteredEvent("Ama", "ama@test.com");

        var emailSender = mock(NotificationSender.class);
        when(senderFactory.create(NotificationChannel.EMAIL)).thenReturn(emailSender);
        when(emailSender.send(any())).thenReturn(
                new NotificationResult(NotificationChannel.EMAIL, true, "OK", 1)
        );

        listener.onUserRegistered(event);

        verify(senderFactory).create(NotificationChannel.EMAIL);
        verify(senderFactory, never()).create(NotificationChannel.SMS);
    }
}
