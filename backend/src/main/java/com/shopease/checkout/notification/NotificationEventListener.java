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
import org.springframework.transaction.event.TransactionalEventListener;

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

    /**
    * This (@TransactionalEventListener) tells Spring to fire the event after the transaction commits,
    * so the order is guaranteed to exist in the database when the async
    * listener runs.
    */
    @Async
    @TransactionalEventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for order: {}", event.orderNumber());

        String plainText = "Thank you, %s! Your order #%s for %s %.2f has been confirmed. Payment via %s (Txn: %s).".formatted(
                event.userName(), event.orderNumber(),
                event.currency().name(), event.total(),
                event.paymentMethod(), event.transactionId()
        );

        String html = buildOrderConfirmationHtml(
                event.userName(), event.orderNumber(),
                event.currency().name(), event.total(),
                event.paymentMethod(), event.transactionId()
        );

        var payload = new NotificationPayload(
                event.userId(),
                event.userEmail(),
                event.userPhone(),
                "Order Confirmation — #" + event.orderNumber(),
                plainText,
                html,
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

        String plainText = "Hi %s, welcome to ShopEase! Your account has been created successfully. Browse our catalog and enjoy shopping!".formatted(event.userName());

        String html = buildWelcomeHtml(event.userName());

        var payload = new NotificationPayload(
                null,
                event.userEmail(),
                null,
                "Welcome to ShopEase!",
                plainText,
                html,
                null
        );

        // Welcome email is always sent via EMAIL — not preference-based
        var emailSender = senderFactory.create(NotificationChannel.EMAIL);
        var result = emailSender.send(payload);
        log.info("Welcome email to {}: success={}", event.userEmail(), result.success());
    }

    // ─── HTML Email Templates ────────────────────────────

    private String buildWelcomeHtml(String userName) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f4f4f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <!-- Header -->
                        <tr>
                          <td style="background:#18181b;padding:32px 40px;text-align:center;">
                            <div style="display:inline-block;width:48px;height:48px;line-height:48px;background:#6366f1;color:#fff;font-size:22px;font-weight:700;border-radius:12px;text-align:center;">S</div>
                            <h1 style="color:#ffffff;font-size:22px;margin:16px 0 0;">Welcome to ShopEase!</h1>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:40px;">
                            <p style="font-size:16px;color:#18181b;margin:0 0 16px;">Hi <strong>%s</strong>,</p>
                            <p style="font-size:15px;color:#3f3f46;line-height:1.6;margin:0 0 24px;">
                              Your account has been created successfully. You're all set to explore our catalog and enjoy a seamless checkout experience.
                            </p>
                            <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                              <tr><td style="background:#6366f1;border-radius:8px;">
                                <a href="http://localhost:3000" style="display:inline-block;padding:14px 32px;color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;">Start Shopping</a>
                              </td></tr>
                            </table>
                            <hr style="border:none;border-top:1px solid #e4e4e7;margin:32px 0;">
                            <p style="font-size:13px;color:#a1a1aa;margin:0;">
                              As a member, you can enjoy tier-based shipping discounts and multiple payment options. Happy shopping!
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#fafafa;padding:24px 40px;text-align:center;border-top:1px solid #e4e4e7;">
                            <p style="font-size:12px;color:#a1a1aa;margin:0;">ShopEase Checkout &mdash; Design Patterns Demo</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(userName);
    }

    private String buildOrderConfirmationHtml(String userName, String orderNumber,
                                               String currency, double total,
                                               String paymentMethod, String transactionId) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#f4f4f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.1);">
                        <!-- Header -->
                        <tr>
                          <td style="background:#18181b;padding:32px 40px;text-align:center;">
                            <div style="display:inline-block;width:48px;height:48px;line-height:48px;background:#6366f1;color:#fff;font-size:22px;font-weight:700;border-radius:12px;text-align:center;">S</div>
                            <h1 style="color:#ffffff;font-size:22px;margin:16px 0 0;">Order Confirmed!</h1>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:40px;">
                            <p style="font-size:16px;color:#18181b;margin:0 0 16px;">Hi <strong>%s</strong>,</p>
                            <p style="font-size:15px;color:#3f3f46;line-height:1.6;margin:0 0 24px;">
                              Thank you for your purchase! Your order has been confirmed and is being processed.
                            </p>
                            <!-- Order Details Card -->
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fafafa;border:1px solid #e4e4e7;border-radius:8px;margin:0 0 24px;">
                              <tr>
                                <td style="padding:20px;">
                                  <table width="100%%" cellpadding="0" cellspacing="0">
                                    <tr>
                                      <td style="font-size:13px;color:#71717a;padding:4px 0;">Order Number</td>
                                      <td style="font-size:14px;color:#18181b;font-weight:600;text-align:right;padding:4px 0;font-family:monospace;">%s</td>
                                    </tr>
                                    <tr>
                                      <td colspan="2" style="border-top:1px solid #e4e4e7;padding:0;height:8px;"></td>
                                    </tr>
                                    <tr>
                                      <td style="font-size:13px;color:#71717a;padding:4px 0;">Payment</td>
                                      <td style="font-size:14px;color:#18181b;text-align:right;padding:4px 0;">%s</td>
                                    </tr>
                                    <tr>
                                      <td colspan="2" style="border-top:1px solid #e4e4e7;padding:0;height:8px;"></td>
                                    </tr>
                                    <tr>
                                      <td style="font-size:13px;color:#71717a;padding:4px 0;">Transaction ID</td>
                                      <td style="font-size:13px;color:#71717a;text-align:right;padding:4px 0;font-family:monospace;">%s</td>
                                    </tr>
                                    <tr>
                                      <td colspan="2" style="border-top:1px solid #e4e4e7;padding:0;height:12px;"></td>
                                    </tr>
                                    <tr>
                                      <td style="font-size:15px;color:#18181b;font-weight:700;padding:4px 0;">Total</td>
                                      <td style="font-size:18px;color:#18181b;font-weight:700;text-align:right;padding:4px 0;">%s %.2f</td>
                                    </tr>
                                  </table>
                                </td>
                              </tr>
                            </table>
                            <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                              <tr><td style="background:#6366f1;border-radius:8px;">
                                <a href="http://localhost:3000/confirmation/%s" style="display:inline-block;padding:14px 32px;color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;">View Order</a>
                              </td></tr>
                            </table>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#fafafa;padding:24px 40px;text-align:center;border-top:1px solid #e4e4e7;">
                            <p style="font-size:12px;color:#a1a1aa;margin:0;">ShopEase Checkout &mdash; Design Patterns Demo</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(userName, orderNumber, paymentMethod, transactionId, currency, total, orderNumber);
    }
}
