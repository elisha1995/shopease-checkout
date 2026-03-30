package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationSenderFactoryTest {

    // Stub senders for testing — real senders need AWS/Hubtel/Slack credentials
    static NotificationSender stubSender(NotificationChannel ch) {
        return new NotificationSender() {
            @Override public NotificationChannel getChannel() { return ch; }
            @Override public NotificationResult send(NotificationPayload p) {
                return new NotificationResult(ch, true, "Stub " + ch, 1);
            }
        };
    }

    private final NotificationSenderFactory factory = new NotificationSenderFactory(List.of(
            stubSender(NotificationChannel.EMAIL),
            stubSender(NotificationChannel.SMS),
            stubSender(NotificationChannel.PUSH),
            stubSender(NotificationChannel.SLACK)
    ));

    @Test void shouldCreateEmailSender() {
        assertEquals(NotificationChannel.EMAIL, factory.create(NotificationChannel.EMAIL).getChannel());
    }

    @Test void shouldCreateSmsSender() {
        assertEquals(NotificationChannel.SMS, factory.create(NotificationChannel.SMS).getChannel());
    }

    @Test void shouldCreatePushSender() {
        assertEquals(NotificationChannel.PUSH, factory.create(NotificationChannel.PUSH).getChannel());
    }

    @Test void shouldCreateSlackSender() {
        assertEquals(NotificationChannel.SLACK, factory.create(NotificationChannel.SLACK).getChannel());
    }

    @Test void shouldReturnEmailAsFallback() {
        assertEquals(NotificationChannel.EMAIL, factory.getEmailFallback().getChannel());
    }
}
