package com.shopease.checkout.order;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.CheckoutResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.mapper.OrderMapper;
import com.shopease.checkout.notification.service.NotificationService;
import com.shopease.checkout.payment.*;
import com.shopease.checkout.repository.NotificationLogRepository;
import com.shopease.checkout.repository.OrderRepository;
import com.shopease.checkout.shipping.ShippingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrator: ties shipping, payment, persistence, and event publishing.
 * 
 * DIP: Depends on interfaces (ShippingService, CurrencyConversionService, PaymentProcessor)
 * not concrete implementations. Observer: publishes events, never imports notification package
 * service layer (only NotificationService for reading results).
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final ShippingService shippingService;
    private final PaymentProcessorFactory paymentFactory;
    private final CurrencyConversionService currencyService;
    private final OrderRepository orderRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderServiceImpl(ShippingService shippingService,
                            PaymentProcessorFactory paymentFactory,
                            CurrencyConversionService currencyService,
                            OrderRepository orderRepository,
                            NotificationLogRepository notificationLogRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.shippingService = shippingService;
        this.paymentFactory = paymentFactory;
        this.currencyService = currencyService;
        this.orderRepository = orderRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public CheckoutResponse checkout(UserEntity user, CheckoutRequest request) {
        // 1. Calculate shipping (delegates to ShippingService → Strategy + Discount Chain)
        var shippingQuote = shippingService.calculate(
                request.shippingMethod(), request.items(), user.getTier());

        // 2. Calculate totals
        double subtotalUsd = request.items().stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();
        double totalUsd = subtotalUsd + shippingQuote.finalCost();

        // 3. Convert currency
        var currency = request.currency() != null ? request.currency() : Currency.USD;
        double totalInCurrency = currencyService.convertFromUsd(totalUsd, currency);

        // 4. Process payment (Factory → Strategy → Adapter)
        var orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        var processor = paymentFactory.create(request.paymentMethod());
        var paymentResult = processor.processPayment(
                new PaymentRequest(orderId, totalInCurrency, currency, user.getEmail()));

        if (!paymentResult.success()) {
            return new CheckoutResponse(false, orderId, "Payment failed: " + paymentResult.providerMessage());
        }

        // 5. Persist order
        var orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setUser(user);
        orderEntity.setSubtotal(BigDecimal.valueOf(subtotalUsd).setScale(2, RoundingMode.HALF_UP));
        orderEntity.setShippingCost(BigDecimal.valueOf(shippingQuote.finalCost()).setScale(2, RoundingMode.HALF_UP));
        orderEntity.setTotal(BigDecimal.valueOf(totalInCurrency).setScale(2, RoundingMode.HALF_UP));
        orderEntity.setCurrency(currency);
        orderEntity.setPaymentMethod(request.paymentMethod());
        orderEntity.setTransactionId(paymentResult.transactionId());
        orderEntity.setShippingMethod(request.shippingMethod());

        request.items().stream()
                .map(OrderMapper::toItemEntity)
                .forEach(orderEntity::addItem);

        orderRepository.save(orderEntity);

        // 6. Publish event (Observer: decoupled from notification logic)
        eventPublisher.publishEvent(new OrderPlacedEvent(
                orderId, user.getId().toString(), user.getFullName(),
                user.getEmail(), user.getPhone(),
                user.getNotificationChannels(),
                totalInCurrency, currency,
                request.paymentMethod(), paymentResult.transactionId()
        ));

        return new CheckoutResponse(true, orderId, "Order placed successfully via " + paymentResult.provider());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> findById(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> OrderMapper.toResponse(order, notificationLogRepository.findByOrderId(orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UserEntity user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(order -> OrderMapper.toResponse(order, notificationLogRepository.findByOrderId(order.getId())))
                .toList();
    }
}
