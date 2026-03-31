package com.shopease.checkout.repository;

import com.shopease.checkout.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<OrderEntity> findByOrderNumber(String orderNumber);
}
