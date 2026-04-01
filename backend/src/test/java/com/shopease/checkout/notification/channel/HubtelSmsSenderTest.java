package com.shopease.checkout.notification.channel;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.NotificationPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HubtelSmsSenderTest {

    private HttpClient httpClient;
    private HubtelSmsSender sender;

    private final NotificationPayload payload = new NotificationPayload(
            "u1", "test@test.com", "+233240000000",
            "Subject", "Your order is confirmed", null, "ORD-001"
    );

    @SuppressWarnings("unchecked")
    private HttpResponse<String> buildMockResponse(int statusCode, String body) {
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(statusCode).when(response).statusCode();
        doReturn(body).when(response).body();
        return response;
    }

    @SuppressWarnings("unchecked")
    private void stubClientToReturn(int statusCode, String body) throws Exception {
        var response = buildMockResponse(statusCode, body);
        doReturn(response).when(httpClient).send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class));
    }

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        sender = new HubtelSmsSender("https://api.hubtel.com/sms", "ShopEase",
                "Basic dGVzdDp0ZXN0", httpClient);
    }

    @Test
    void getChannelReturnsSms() {
        assertEquals(NotificationChannel.SMS, sender.getChannel());
    }

    @Test
    void sendReturnsSuccessOn2xxResponse() throws Exception {
        stubClientToReturn(200, "{\"status\":\"sent\"}");

        var result = sender.send(payload);

        assertTrue(result.success());
        assertEquals(NotificationChannel.SMS, result.channel());
        assertTrue(result.message().contains("+233240000000"));
    }

    @Test
    void sendReturnsSuccessOn201Response() throws Exception {
        stubClientToReturn(201, "{\"status\":\"queued\"}");

        var result = sender.send(payload);
        assertTrue(result.success());
    }

    @Test
    void sendReturnsFailureOnNon2xxResponse() throws Exception {
        stubClientToReturn(401, "Unauthorized");

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("401"));
    }

    @Test
    void sendReturnsFailureOn500Response() throws Exception {
        stubClientToReturn(500, "Internal error");

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("500"));
    }

    @Test
    void sendReturnsFailureWhenNoPhoneNumber() {
        var noPhonePayload = new NotificationPayload(
                null, "test@test.com", null, "Subject",
                "Body", null, "ORD-001"
        );

        var result = sender.send(noPhonePayload);

        assertFalse(result.success());
        assertTrue(result.message().contains("No phone number"));
    }

    @Test
    void sendReturnsFailureWhenPhoneIsBlank() {
        var blankPhonePayload = new NotificationPayload(
                "u1", "test@test.com", "  ", "Subject", "Body",
                null, "ORD-001"
        );

        var result = sender.send(blankPhonePayload);

        assertFalse(result.success());
        assertTrue(result.message().contains("No phone number"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendHandlesInterruptedException() throws Exception {
        doThrow(new InterruptedException("Thread interrupted"))
                .when(httpClient).send(any(HttpRequest.class),
                        any(HttpResponse.BodyHandler.class));

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("interrupted"));
        assertTrue(Thread.currentThread().isInterrupted());
        // Clear interrupted flag for test cleanup
        //noinspection ResultOfMethodCallIgnored
        Thread.interrupted();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendHandlesGenericException() throws Exception {
        doThrow(new RuntimeException("DNS resolution failed"))
                .when(httpClient).send(any(HttpRequest.class),
                        any(HttpResponse.BodyHandler.class));

        var result = sender.send(payload);

        assertFalse(result.success());
        assertTrue(result.message().contains("DNS resolution failed"));
    }

    @Test
    void sendUsesRecipientIdAsFallbackPhone() throws Exception {
        var fallbackPayload = new NotificationPayload(
                "+233550000000", "test@test.com", null, "Subject",
                "Body", null, "ORD-001"
        );
        stubClientToReturn(200, "OK");

        var result = sender.send(fallbackPayload);

        assertTrue(result.success());
    }
}
