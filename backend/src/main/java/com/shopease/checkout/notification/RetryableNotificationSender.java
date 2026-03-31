package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DECORATOR PATTERN + EXTRA WHAT-IF: "What if a channel fails?"
 * <p>
 * Wraps any NotificationSender with retry logic. If retries are exhausted,
 * falls back to Email. The wrapped sender doesn't know it's being retried.
 */
@Component
public class RetryableNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(RetryableNotificationSender.class);

    private final NotificationSenderFactory factory;
    private final int maxRetries;

    public RetryableNotificationSender(NotificationSenderFactory factory, @Value("${notification.max-retries}") int maxRetries) {
        this.factory = factory;
        this.maxRetries = maxRetries;
    }

    public NotificationResult sendWithRetry(NotificationChannel channel, NotificationPayload payload) {
        return sendWithRetry(channel, payload, maxRetries);
    }

    public NotificationResult sendWithRetry(NotificationChannel channel, NotificationPayload payload, int maxRetries) {
        NotificationSender sender = factory.create(channel);
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;
            try {
                NotificationResult result = sender.send(payload);
                if (result.success()) {
                    return new NotificationResult(channel, true, result.message(), attempts);
                }
                log.warn("Attempt {}/{} failed for {} channel: {}",
                        attempts, maxRetries, channel, result.message());
            } catch (Exception e) {
                log.warn("Attempt {}/{} threw exception for {} channel: {}",
                        attempts, maxRetries, channel, e.getMessage());
            }
        }

        // All retries exhausted — fallback to Email if not already Email
        if (channel != NotificationChannel.EMAIL) {
            log.warn("All {} retries exhausted for {}. Falling back to EMAIL.", maxRetries, channel);
            try {
                NotificationSender emailFallback = factory.getEmailFallback();
                NotificationResult fallbackResult = emailFallback.send(payload);
                return new NotificationResult(
                        NotificationChannel.EMAIL,
                        fallbackResult.success(),
                        "Fallback to email after %s failed %d times. %s".formatted(channel, maxRetries, fallbackResult.message()),
                        attempts + 1
                );
            } catch (Exception e) {
                log.error("Email fallback also failed: {}", e.getMessage());
            }
        }

        return new NotificationResult(channel, false,
                "All %d retries exhausted for %s. No fallback available.".formatted(maxRetries, channel),
                attempts);
    }
}
