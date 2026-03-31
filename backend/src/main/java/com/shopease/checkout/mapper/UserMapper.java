package com.shopease.checkout.mapper;

import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.dto.request.RegisterRequest;
import com.shopease.checkout.dto.response.AuthResponse;
import com.shopease.checkout.dto.response.UserProfileResponse;
import com.shopease.checkout.entity.UserEntity;

import java.util.List;

/**
 * Manual mapper — no library dependency, full control.
 * All methods are static and stateless (DRY: single source of truth for conversions).
 */
public final class UserMapper {

    private UserMapper() {
    } // Utility class — not instantiable

    public static UserEntity toEntity(RegisterRequest request, String encodedPassword) {
        var entity = new UserEntity();
        entity.setFullName(request.fullName());
        entity.setEmail(request.email());
        entity.setPasswordHash(encodedPassword);
        entity.setPhone(request.phone());
        entity.setTier(request.tier() != null ? request.tier() : MembershipTier.STANDARD);
        entity.setNotificationChannels(
                request.notificationPreferences() != null
                        ? request.notificationPreferences()
                        : List.of(NotificationChannel.EMAIL)
        );
        return entity;
    }

    public static AuthResponse toAuthResponse(UserEntity entity, String token) {
        return new AuthResponse(
                token,
                entity.getId().toString(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getTier().name()
        );
    }

    public static UserProfileResponse toProfileResponse(UserEntity entity) {
        return new UserProfileResponse(
                entity.getId().toString(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getTier().name(),
                entity.getNotificationChannels().stream().map(Enum::name).toList()
        );
    }
}
