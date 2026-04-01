package com.shopease.checkout.mapper;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.entity.NotificationLogEntity;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.entity.OrderItemEntity;
import com.shopease.checkout.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    @Test
    void toItemEntityMapsCartItemCorrectly() {
        var dto = new CartItemDto("prod-1", "Wireless Earbuds", 29.99, 2);
        var entity = OrderMapper.toItemEntity(dto);

        assertEquals("Wireless Earbuds", entity.getProductName());
        assertEquals(BigDecimal.valueOf(29.99), entity.getPrice());
        assertEquals(2, entity.getQuantity());
    }

    @Test
    void toItemResponseMapsEntityCorrectly() {
        var entity = new OrderItemEntity();
        entity.setProductName("Laptop Stand");
        entity.setPrice(BigDecimal.valueOf(45.00));
        entity.setQuantity(1);

        var response = OrderMapper.toItemResponse(entity);

        assertEquals("Laptop Stand", response.productName());
        assertEquals(45.00, response.price());
        assertEquals(1, response.quantity());
    }

    @Test
    void toNotificationResponseMapsCorrectly() {
        var entity = new NotificationLogEntity();
        entity.setChannel(NotificationChannel.EMAIL);
        entity.setSuccess(true);
        entity.setMessage("Email sent via AWS SES");
        entity.setAttempts(1);

        var response = OrderMapper.toNotificationResponse(entity);

        assertEquals("EMAIL", response.channel());
        assertTrue(response.success());
        assertEquals("Email sent via AWS SES", response.message());
        assertEquals(1, response.attempts());
    }

    @Test
    void toResponseMapsFullOrderCorrectly() {
        var user = new UserEntity();
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.fromString("019d0000-0000-7000-8000-000000000001"));
        } catch (Exception _) {
            fail("Could not set user ID");
        }

        var item = new OrderItemEntity();
        item.setProductName("Earbuds");
        item.setPrice(BigDecimal.valueOf(29.99));
        item.setQuantity(2);

        var order = new OrderEntity();
        order.setOrderNumber("ORD-20260401-ABCD");
        order.setUser(user);
        order.setSubtotal(BigDecimal.valueOf(59.98));
        order.setShippingCost(BigDecimal.valueOf(6.49));
        order.setTotal(BigDecimal.valueOf(66.47));
        order.setCurrency(Currency.USD);
        order.setPaymentMethod("STRIPE");
        order.setTransactionId("ch_abc123");
        order.setShippingMethod("STANDARD");
        order.addItem(item);

        var notifLog = new NotificationLogEntity();
        notifLog.setChannel(NotificationChannel.EMAIL);
        notifLog.setSuccess(true);
        notifLog.setMessage("Sent");
        notifLog.setAttempts(1);

        var response = OrderMapper.toResponse(order, List.of(notifLog));

        assertEquals("ORD-20260401-ABCD", response.orderNumber());
        assertEquals("019d0000-0000-7000-8000-000000000001", response.userId());
        assertEquals(1, response.items().size());
        assertEquals("Earbuds", response.items().getFirst().productName());
        assertEquals(59.98, response.subtotal());
        assertEquals(6.49, response.shippingCost());
        assertEquals(66.47, response.total());
        assertEquals("USD", response.currency());
        assertEquals("STRIPE", response.paymentMethod());
        assertEquals("ch_abc123", response.transactionId());
        assertEquals("STANDARD", response.shippingMethod());
        assertEquals(1, response.notifications().size());
        assertTrue(response.notifications().getFirst().success());
    }
}
