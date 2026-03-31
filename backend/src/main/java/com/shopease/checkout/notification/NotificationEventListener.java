package com.shopease.checkout.notification;

import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.notification.service.NotificationService;
import com.shopease.checkout.order.OrderPlacedEvent;
import com.shopease.checkout.security.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN: Listens for domain events and delegates to NotificationService.
 * <p>
 * This class has ZERO imports from the order/auth service layers.
 * It depends only on event records — true decoupling (DIP).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final NotificationSenderFactory senderFactory;

    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for order: {}", event.orderNumber());

        var payload = new NotificationPayload(
                event.userId(),
                event.userEmail(),
                event.userPhone(),
                "Order Confirmation — #" + event.orderNumber(),
                """
                        Thank you, %s! Your order #%s for %s %.2f has been confirmed. \
                        Payment via %s (Txn: %s).\
                        """.formatted(
                        event.userName(), event.orderNumber(),
                        event.currency().name(), event.total(),
                        event.paymentMethod(), event.transactionId()
                ),
                event.orderNumber()
        );

        notificationService.sendWithRetryAndPersist(
                event.orderId(), event.notificationChannels(), payload
        );
    }

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.userEmail());

        var payload = new NotificationPayload(
                null,
                event.userEmail(),
                null,
                "Welcome to ShopEase!",
                """
                        Hi %s, welcome to ShopEase! Your account has been created successfully. \
                        Browse our catalog and enjoy shopping!\
                        """.formatted(event.userName()),
                null
        );

        // Welcome email is always sent via EMAIL — not preference-based
        var emailSender = senderFactory.create(NotificationChannel.EMAIL);
        var result = emailSender.send(payload);
        log.info("Welcome email to {}: success={}", event.userEmail(), result.success());
    }
}
