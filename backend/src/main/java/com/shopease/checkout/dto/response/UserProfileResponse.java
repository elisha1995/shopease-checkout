package com.shopease.checkout.dto.response;

public record UserProfileResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String tier
) {
}
