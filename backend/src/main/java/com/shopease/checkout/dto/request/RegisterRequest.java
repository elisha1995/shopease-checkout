package com.shopease.checkout.dto.request;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.common.model.NotificationChannel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,
        
        String phone,
        MembershipTier tier,
        List<NotificationChannel> notificationPreferences
) {
}
