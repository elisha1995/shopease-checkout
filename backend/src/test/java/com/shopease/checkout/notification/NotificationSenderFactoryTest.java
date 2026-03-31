package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationSenderFactoryTest {

    // Stub senders for testing — real senders need AWS/Hubtel credentials
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
            stubSender(NotificationChannel.SMS)
    ));

    @Test void shouldCreateEmailSender() {
        assertEquals(NotificationChannel.EMAIL, factory.create(NotificationChannel.EMAIL).getChannel());
    }

    @Test void shouldCreateSmsSender() {
        assertEquals(NotificationChannel.SMS, factory.create(NotificationChannel.SMS).getChannel());
    }

    @Test void shouldReturnEmailAsFallback() {
        assertEquals(NotificationChannel.EMAIL, factory.getEmailFallback().getChannel());
    }
}
