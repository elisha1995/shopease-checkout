package com.shopease.checkout.repository;

import com.shopease.checkout.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {
    List<NotificationLogEntity> findByOrderId(UUID orderId);
}
