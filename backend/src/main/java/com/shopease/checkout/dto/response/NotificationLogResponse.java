package com.shopease.checkout.dto.response;

public record NotificationLogResponse(
        String channel,
        boolean success,
        String message,
        int attempts
) {
}
