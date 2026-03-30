package com.shopease.checkout.entity;

import jakarta.persistence.*;
import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.common.model.NotificationChannel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

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

    // ─── Getters & Setters ────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }

    public String getNotificationPreferences() {
        return notificationPreferences;
    }

    public void setNotificationPreferences(String notificationPreferences) {
        this.notificationPreferences = notificationPreferences;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
