package com.shopease.checkout.notification.channel;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.NotificationPayload;
import com.shopease.checkout.notification.NotificationResult;
import com.shopease.checkout.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Real Hubtel SMS integration.
 * API docs: https://developers.hubtel.com/reference/send-message
 */
@Component
public class HubtelSmsSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(HubtelSmsSender.class);

    private final String baseUrl;
    private final String from;
    private final String authHeader;
    private final HttpClient httpClient;

    public HubtelSmsSender(
            @Value("${hubtel.base-url}") String baseUrl,
            @Value("${hubtel.client-id}") String clientId,
            @Value("${hubtel.client-secret}") String clientSecret,
            @Value("${hubtel.from}") String from) {
        this.baseUrl = baseUrl;
        this.from = from;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        // The recipient phone number comes from the user's profile.
        // For now, we'll use recipientId as a fallback if phone isn't in payload.
        String phone = payload.recipientPhone() != null ? payload.recipientPhone() : payload.recipientId();

        if (phone == null || phone.isBlank()) {
            log.warn("[HUBTEL] No phone number for user: {}", payload.recipientId());
            return new NotificationResult(
                    NotificationChannel.SMS, false, "No phone number available", 1
            );
        }

        try {
            String json = """
                    {
                        "From": "%s",
                        "To": "%s",
                        "Content": "%s"
                    }
                    """.formatted(from, phone, escapeJson(payload.body()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[HUBTEL] SMS sent to {} | Status: {}", phone, response.statusCode());
                return new NotificationResult(
                        NotificationChannel.SMS, true,
                        "SMS sent via Hubtel to " + phone, 1
                );
            } else {
                log.error("[HUBTEL] SMS failed | Status: {} | Body: {}", response.statusCode(), response.body());
                return new NotificationResult(
                        NotificationChannel.SMS, false,
                        "Hubtel returned " + response.statusCode() + ": " + response.body(), 1
                );
            }
        } catch (Exception e) {
            log.error("[HUBTEL] SMS error: {}", e.getMessage());
            return new NotificationResult(
                    NotificationChannel.SMS, false,
                    "SMS failed: " + e.getMessage(), 1
            );
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n");
    }
}
