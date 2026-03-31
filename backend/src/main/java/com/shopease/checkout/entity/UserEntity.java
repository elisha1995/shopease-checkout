package com.shopease.checkout.entity;

import com.shopease.checkout.common.config.UUIDv7;
import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.common.model.NotificationChannel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @UUIDv7
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipTier tier = MembershipTier.STANDARD;

    @Column(name = "notification_preferences", nullable = false)
    private String notificationPreferences = "EMAIL";

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    // ─── Helpers ──────────────────────────────────────

    public List<NotificationChannel> getNotificationChannels() {
        if (notificationPreferences == null || notificationPreferences.isBlank()) {
            return List.of(NotificationChannel.EMAIL);
        }
        return java.util.Arrays.stream(notificationPreferences.split(","))
                .map(String::trim)
                .map(NotificationChannel::valueOf)
                .toList();
    }

    public void setNotificationChannels(List<NotificationChannel> channels) {
        this.notificationPreferences = channels.stream()
                .map(Enum::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("EMAIL");
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
