package com.shopease.checkout.entity;

import com.shopease.checkout.common.config.UUIDv7;
import com.shopease.checkout.common.model.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {

    @Id
    @UUIDv7
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.USD;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "shipping_method", nullable = false)
    private String shippingMethod;

    @Column(nullable = false)
    private String status = "CONFIRMED";

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Setter(lombok.AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Setter(lombok.AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationLogEntity> notifications = new ArrayList<>();

    // ─── Helpers ──────────────────────────────────────

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}