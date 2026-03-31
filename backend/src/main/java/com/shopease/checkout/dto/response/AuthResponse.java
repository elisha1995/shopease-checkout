package com.shopease.checkout.dto.response;

public record AuthResponse(
        String token,
        String userId,
        String fullName,
        String email,
        String tier
) {
}
