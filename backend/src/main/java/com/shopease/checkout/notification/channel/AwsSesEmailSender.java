package com.shopease.checkout.notification.channel;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.NotificationPayload;
import com.shopease.checkout.notification.NotificationResult;
import com.shopease.checkout.notification.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

@Component
public class AwsSesEmailSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(AwsSesEmailSender.class);

    private final SesClient sesClient;
    private final String fromEmail;

    public AwsSesEmailSender(
            @Value("${aws.ses.region}") String region,
            @Value("${aws.ses.from-email}") String fromEmail) {
        this.sesClient = SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
        this.fromEmail = fromEmail;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(d -> d.toAddresses(payload.recipientEmail()))
                    .message(m -> m
                            .subject(s -> s.data(payload.subject()).charset("UTF-8"))
                            .body(b -> b
                                    .text(t -> t.data(payload.body()).charset("UTF-8"))))
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            String messageId = response.messageId();

            log.info("[AWS SES] Email sent to {} | MessageId: {}", payload.recipientEmail(), messageId);

            return new NotificationResult(
                    NotificationChannel.EMAIL, true,
                    "Email sent via AWS SES (MessageId: " + messageId + ")", 1
            );
        } catch (SesException e) {
            log.error("[AWS SES] Failed to send email to {}: {}", payload.recipientEmail(), e.awsErrorDetails().errorMessage());
            return new NotificationResult(
                    NotificationChannel.EMAIL, false,
                    "SES error: " + e.awsErrorDetails().errorMessage(), 1
            );
        } catch (Exception e) {
            log.error("[AWS SES] Unexpected error: {}", e.getMessage());
            return new NotificationResult(
                    NotificationChannel.EMAIL, false,
                    "Email failed: " + e.getMessage(), 1
            );
        }
    }
}
