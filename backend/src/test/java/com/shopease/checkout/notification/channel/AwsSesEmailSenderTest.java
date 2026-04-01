package com.shopease.checkout.notification.channel;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.NotificationPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsSesEmailSenderTest {

    private SesClient sesClient;
    private AwsSesEmailSender sender;

    private final NotificationPayload payload = new NotificationPayload(
            "u1", "test@test.com", null, "Test Subject",
            "Test body", null, "ORD-001"
    );

    private final NotificationPayload htmlPayload = new NotificationPayload(
            "u1", "test@test.com", null, "Test Subject",
            "Test body", "<h1>Hello</h1>", "ORD-001"
    );

    @BeforeEach
    void setUp() {
        sesClient = mock(SesClient.class);
        sender = new AwsSesEmailSender(sesClient, "noreply@shopease.dev");
    }

    @Test
    void getChannelReturnsEmail() {
        assertEquals(NotificationChannel.EMAIL, sender.getChannel());
    }

    @Test
    void sendReturnsSuccessOnValidResponse() {
        var response = SendEmailResponse.builder().messageId("msg-123").build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        var result = sender.send(payload);

        assertTrue(result.success());
        assertEquals(NotificationChannel.EMAIL, result.channel());
        assertTrue(result.message().contains("msg-123"));
        assertEquals(1, result.attempts());
    }

    @Test
    void sendIncludesHtmlBodyWhenProvided() {
        var response = SendEmailResponse.builder().messageId("msg-456").build();
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(response);

        var result = sender.send(htmlPayload);

        assertTrue(result.success());
        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void sendReturnsFailureOnSesException() {
        var errorDetails = AwsErrorDetails.builder()
                .errorMessage("Email address is not verified")
                .build();
        var sesException = (SesException) SesException.builder()
                .awsErrorDetails(errorDetails)
                .message("SES error")
                .build();
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(sesException);

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("SES error"));
        assertTrue(result.message().contains("not verified"));
    }

    @Test
    void sendReturnsFailureOnUnexpectedException() {
        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("Connection timeout"));
    }
}
