package com.shopease.checkout.order;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.CheckoutResponse;
import com.shopease.checkout.dto.response.OrderResponse;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.mapper.OrderMapper;
import com.shopease.checkout.payment.CurrencyConversionService;
import com.shopease.checkout.payment.PaymentProcessorFactory;
import com.shopease.checkout.payment.PaymentRequest;
import com.shopease.checkout.repository.NotificationLogRepository;
import com.shopease.checkout.repository.OrderRepository;
import com.shopease.checkout.shipping.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrator: ties shipping, payment, persistence, and event publishing.
 * <p>
 * DIP: Depends on interfaces (ShippingService, CurrencyConversionService, PaymentProcessor)
 * not concrete implementations. Observer: publishes events, never imports notification package
 * service layer.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShippingService shippingService;
    private final PaymentProcessorFactory paymentFactory;
    private final CurrencyConversionService currencyService;
    private final OrderRepository orderRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // 4. Generate order number
        var orderNumber = generateOrderNumber();

        // 5. Process payment (Factory → Strategy → Adapter)
        var processor = paymentFactory.create(request.paymentMethod());
        var paymentResult = processor.processPayment(
                new PaymentRequest(orderNumber, totalInCurrency, currency, user.getEmail()));

        if (!paymentResult.success()) {
            return new CheckoutResponse(false, orderNumber, "Payment failed: " + paymentResult.providerMessage());
        }

        // 6. Persist order
        var orderEntity = new OrderEntity();
        orderEntity.setOrderNumber(orderNumber);
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

        // 7. Publish event (Observer: decoupled from notification logic)
        eventPublisher.publishEvent(new OrderPlacedEvent(
                orderEntity.getId(), orderNumber,
                user.getId().toString(), user.getFullName(),
                user.getEmail(), user.getPhone(),
                user.getNotificationChannels(),
                totalInCurrency, currency,
                request.paymentMethod(), paymentResult.transactionId()
        ));

        return new CheckoutResponse(true, orderNumber, "Order placed successfully via " + paymentResult.provider());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(order -> OrderMapper.toResponse(order, notificationLogRepository.findByOrderId(order.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UserEntity user) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(order -> OrderMapper.toResponse(order, notificationLogRepository.findByOrderId(order.getId())))
                .toList();
    }

    private String generateOrderNumber() {
        var datePart = LocalDate.now().format(DATE_FMT);
        var suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            suffix.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return "ORD-%s-%s".formatted(datePart, suffix);
    }
}
