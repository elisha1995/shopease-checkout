package com.shopease.checkout.dto.response;

import java.util.List;

public record UserProfileResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String tier,
        List<String> notificationPreferences
) {}
