package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RetryableNotificationSenderTest {

    private final NotificationPayload payload = new NotificationPayload(
            "u1", "test@test.com", "+233200000000", "Test", "Test body", "ORD-001"
    );

    static NotificationSender stubSender(NotificationChannel ch) {
        return new NotificationSender() {
            @Override public NotificationChannel getChannel() { return ch; }
            @Override public NotificationResult send(NotificationPayload p) {
                return new NotificationResult(ch, true, "OK", 1);
            }
        };
    }

    @Test void shouldSucceedOnFirstAttempt() {
        var factory = new NotificationSenderFactory(List.of(stubSender(NotificationChannel.EMAIL)));
        var retryable = new RetryableNotificationSender(factory, 3);
        NotificationResult result = retryable.sendWithRetry(NotificationChannel.EMAIL, payload);
        assertTrue(result.success());
        assertEquals(1, result.attempts());
    }

    @Test void shouldRetryOnFailureAndFallbackToEmail() {
        NotificationSender failingSms = new NotificationSender() {
            @Override public NotificationChannel getChannel() { return NotificationChannel.SMS; }
            @Override public NotificationResult send(NotificationPayload p) {
                return new NotificationResult(NotificationChannel.SMS, false, "SMS down", 1);
            }
        };
        var factory = new NotificationSenderFactory(List.of(failingSms, stubSender(NotificationChannel.EMAIL)));
        var retryable = new RetryableNotificationSender(factory, 3);
        NotificationResult result = retryable.sendWithRetry(NotificationChannel.SMS, payload);
        assertTrue(result.success());
        assertEquals(NotificationChannel.EMAIL, result.channel());
        assertEquals(4, result.attempts());
        assertTrue(result.message().contains("Fallback"));
    }

    @Test void shouldRetryExactlyMaxTimes() {
        int[] callCount = {0};
        NotificationSender countingSender = new NotificationSender() {
            @Override public NotificationChannel getChannel() { return NotificationChannel.SMS; }
            @Override public NotificationResult send(NotificationPayload p) {
                callCount[0]++;
                return new NotificationResult(NotificationChannel.SMS, false, "Failed", 1);
            }
        };
        var factory = new NotificationSenderFactory(List.of(countingSender, stubSender(NotificationChannel.EMAIL)));
        var retryable = new RetryableNotificationSender(factory, 3);
        retryable.sendWithRetry(NotificationChannel.SMS, payload);
        assertEquals(3, callCount[0]);
    }

    @Test void shouldHandleExceptionInSender() {
        NotificationSender throwingSender = new NotificationSender() {
            @Override public NotificationChannel getChannel() { return NotificationChannel.SMS; }
            @Override public NotificationResult send(NotificationPayload p) {
                throw new RuntimeException("Connection refused");
            }
        };
        var factory = new NotificationSenderFactory(List.of(throwingSender, stubSender(NotificationChannel.EMAIL)));
        var retryable = new RetryableNotificationSender(factory, 2);
        NotificationResult result = retryable.sendWithRetry(NotificationChannel.SMS, payload);
        assertTrue(result.success());
        assertEquals(NotificationChannel.EMAIL, result.channel());
    }

    @Test void emailFailureDoesNotFallbackToItself() {
        NotificationSender failingEmail = new NotificationSender() {
            @Override public NotificationChannel getChannel() { return NotificationChannel.EMAIL; }
            @Override public NotificationResult send(NotificationPayload p) {
                return new NotificationResult(NotificationChannel.EMAIL, false, "SMTP down", 1);
            }
        };
        var factory = new NotificationSenderFactory(List.of(failingEmail));
        var retryable = new RetryableNotificationSender(factory, 2);
        NotificationResult result = retryable.sendWithRetry(NotificationChannel.EMAIL, payload);
        assertFalse(result.success());
        assertEquals(2, result.attempts());
    }
}
