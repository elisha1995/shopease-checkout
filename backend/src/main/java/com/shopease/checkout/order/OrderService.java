package com.shopease.checkout.order;

import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.CheckoutResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    CheckoutResponse checkout(UserEntity user, CheckoutRequest request);

    Optional<OrderResponse> findByOrderNumber(String orderNumber);

    List<OrderResponse> findByUser(UserEntity user);
}
