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

/**
 * Real Slack integration via incoming webhooks.
 * Set up at: https://api.slack.com/messaging/webhooks
 */
@Component
public class SlackWebhookSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SlackWebhookSender.class);

    private final String webhookUrl;
    private final HttpClient httpClient;

    public SlackWebhookSender(@Value("${slack.webhook-url}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SLACK;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("[SLACK] No webhook URL configured, skipping");
            return new NotificationResult(
                    NotificationChannel.SLACK, false, "Slack webhook URL not configured", 1
            );
        }

        try {
            String slackMessage = """
                    {
                        "blocks": [
                            {
                                "type": "header",
                                "text": { "type": "plain_text", "text": "%s" }
                            },
                            {
                                "type": "section",
                                "text": { "type": "mrkdwn", "text": "%s" }
                            },
                            {
                                "type": "context",
                                "elements": [
                                    { "type": "mrkdwn", "text": "Order: *%s* | Recipient: %s" }
                                ]
                            }
                        ]
                    }
                    """.formatted(
                    escapeJson(payload.subject()),
                    escapeJson(payload.body()),
                    payload.orderId(),
                    payload.recipientEmail()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(slackMessage))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("[SLACK] Message sent for order {}", payload.orderId());
                return new NotificationResult(
                        NotificationChannel.SLACK, true,
                        "Slack notification sent for order " + payload.orderId(), 1
                );
            } else {
                log.error("[SLACK] Failed | Status: {} | Body: {}", response.statusCode(), response.body());
                return new NotificationResult(
                        NotificationChannel.SLACK, false,
                        "Slack returned " + response.statusCode(), 1
                );
            }
        } catch (Exception e) {
            log.error("[SLACK] Error: {}", e.getMessage());
            return new NotificationResult(
                    NotificationChannel.SLACK, false,
                    "Slack failed: " + e.getMessage(), 1
            );
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n");
    }
}
