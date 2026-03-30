package com.shopease.checkout.notification;

import com.shopease.checkout.notification.service.NotificationService;
import com.shopease.checkout.order.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * OBSERVER PATTERN: Listens for OrderPlacedEvent and delegates to NotificationService.
 * 
 * This class has ZERO imports from the order service layer.
 * It depends only on the event record — true decoupling (DIP).
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for order: {}", event.orderId());

        var payload = new NotificationPayload(
                event.userId(),
                event.userEmail(),
                event.userPhone(),
                "Order Confirmation — #" + event.orderId(),
                """
                Thank you, %s! Your order #%s for %s %.2f has been confirmed. \
                Payment via %s (Txn: %s).\
                """.formatted(
                        event.userName(), event.orderId(),
                        event.currency().name(), event.total(),
                        event.paymentMethod(), event.transactionId()
                ),
                event.orderId()
        );

        notificationService.sendWithRetryAndPersist(
                event.orderId(), event.notificationChannels(), payload
        );
    }
}
