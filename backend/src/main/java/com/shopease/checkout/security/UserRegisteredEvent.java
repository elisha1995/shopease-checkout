package com.shopease.checkout.security;

/**
 * OBSERVER PATTERN: Published when a new user registers.
 * Triggers a welcome email via the notification event listener.
 */
public record UserRegisteredEvent(
        String userName,
        String userEmail
) {
}
