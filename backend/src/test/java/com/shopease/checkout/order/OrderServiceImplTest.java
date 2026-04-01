package com.shopease.checkout.order;

import com.shopease.checkout.common.model.Currency;
import com.shopease.checkout.common.model.MembershipTier;
import com.shopease.checkout.common.model.NotificationChannel;
import com.shopease.checkout.dto.request.CartItemDto;
import com.shopease.checkout.dto.request.CheckoutRequest;
import com.shopease.checkout.dto.response.ShippingQuoteResponse;
import com.shopease.checkout.entity.OrderEntity;
import com.shopease.checkout.entity.UserEntity;
import com.shopease.checkout.payment.*;
import com.shopease.checkout.repository.NotificationLogRepository;
import com.shopease.checkout.repository.OrderRepository;
import com.shopease.checkout.shipping.ShippingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private ShippingService shippingService;
    private PaymentProcessorFactory paymentFactory;
    private CurrencyConversionService currencyService;
    private OrderRepository orderRepository;
    private NotificationLogRepository notificationLogRepository;
    private ApplicationEventPublisher eventPublisher;
    private OrderServiceImpl service;

    private UserEntity user;

    private static void setId(Object entity, UUID id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

    @BeforeEach
    void setUp() {
        shippingService = mock(ShippingService.class);
        paymentFactory = mock(PaymentProcessorFactory.class);
        currencyService = mock(CurrencyConversionService.class);
        orderRepository = mock(OrderRepository.class);
        notificationLogRepository = mock(NotificationLogRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new OrderServiceImpl(
                shippingService, paymentFactory, currencyService,
                orderRepository, notificationLogRepository, eventPublisher
        );

        user = new UserEntity();
        user.setEmail("kwame@test.com");
        user.setFullName("Kwame");
        user.setTier(MembershipTier.STANDARD);
        setId(user, UUID.randomUUID());
    }

    private CheckoutRequest makeRequest(List<CartItemDto> items, String paymentMethod,
                                        String shippingMethod, Currency currency,
                                        List<NotificationChannel> channels) {
        return new CheckoutRequest(items, paymentMethod, shippingMethod, currency, channels);
    }

    @Test
    void checkoutSuccessfullyPlacesOrder() {
        var items = List.of(new CartItemDto("p1", "Earbuds",
                29.99, 2));
        var request = makeRequest(items, "STRIPE", "STANDARD",
                Currency.USD, List.of(NotificationChannel.EMAIL));

        when(shippingService.calculate("STANDARD", items, MembershipTier.STANDARD))
                .thenReturn(new ShippingQuoteResponse("STANDARD", "Standard Shipping",
                        6.49, 6.49, List.of()));
        when(currencyService.convertFromUsd(anyDouble(), eq(Currency.USD)))
                .thenAnswer(inv -> inv.getArgument(0));

        var processor = mock(PaymentProcessor.class);
        when(paymentFactory.create("STRIPE")).thenReturn(processor);
        when(processor.processPayment(any(PaymentRequest.class)))
                .thenReturn(new PaymentResult(true, "ch_abc123",
                        "Payment succeeded", "Stripe"));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            var entity = inv.getArgument(0, OrderEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });

        var response = service.checkout(user, request);

        assertTrue(response.success());
        assertNotNull(response.orderNumber());
        assertTrue(response.orderNumber().startsWith("ORD-"));
        assertTrue(response.message().contains("Stripe"));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void checkoutReturnsFailureWhenPaymentFails() {
        var items = List.of(new CartItemDto("p1", "Keyboard",
                89.99, 1));
        var request = makeRequest(items, "STRIPE", "EXPRESS",
                Currency.USD, List.of(NotificationChannel.EMAIL));

        when(shippingService.calculate("EXPRESS", items, MembershipTier.STANDARD))
                .thenReturn(new ShippingQuoteResponse("EXPRESS", "Express Shipping", 15.99,
                        15.99, List.of()));
        when(currencyService.convertFromUsd(anyDouble(), eq(Currency.USD)))
                .thenAnswer(inv -> inv.getArgument(0));

        var processor = mock(PaymentProcessor.class);
        when(paymentFactory.create("STRIPE")).thenReturn(processor);
        when(processor.processPayment(any()))
                .thenReturn(new PaymentResult(false, null, "Card declined",
                        "Stripe"));

        var response = service.checkout(user, request);

        assertFalse(response.success());
        assertTrue(response.message().contains("Payment failed"));
        assertTrue(response.message().contains("Card declined"));
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void checkoutPublishesEventWithCorrectData() {
        var items = List.of(new CartItemDto("p1", "Mouse", 25.00, 1));
        var channels = List.of(NotificationChannel.EMAIL, NotificationChannel.SMS);
        var request = makeRequest(items, "PAYPAL", "STANDARD",
                Currency.USD, channels);

        when(shippingService.calculate("STANDARD", items, MembershipTier.STANDARD))
                .thenReturn(new ShippingQuoteResponse("STANDARD", "Standard", 6.49,
                        6.49, List.of()));
        when(currencyService.convertFromUsd(anyDouble(), eq(Currency.USD)))
                .thenAnswer(inv -> inv.getArgument(0));

        var processor = mock(PaymentProcessor.class);
        when(paymentFactory.create("PAYPAL")).thenReturn(processor);
        when(processor.processPayment(any()))
                .thenReturn(new PaymentResult(true, "PAY-xyz", "OK",
                        "PayPal"));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            var entity = inv.getArgument(0, OrderEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });

        service.checkout(user, request);

        var captor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        var event = captor.getValue();

        assertEquals("Kwame", event.userName());
        assertEquals("kwame@test.com", event.userEmail());
        assertEquals(channels, event.notificationChannels());
        assertEquals("PAYPAL", event.paymentMethod());
        assertEquals("PAY-xyz", event.transactionId());
        assertEquals(Currency.USD, event.currency());
    }

    @Test
    void checkoutConvertsCurrencyWhenNotUsd() {
        var items = List.of(new CartItemDto("p1", "Cable",
                10.00, 1));
        var request = makeRequest(items, "STRIPE", "STANDARD",
                Currency.GHS, List.of(NotificationChannel.EMAIL));

        when(shippingService.calculate("STANDARD", items, MembershipTier.STANDARD))
                .thenReturn(new ShippingQuoteResponse("STANDARD", "Standard", 6.49,
                        6.49, List.of()));
        when(currencyService.convertFromUsd(anyDouble(), eq(Currency.GHS)))
                .thenReturn(247.35);

        var processor = mock(PaymentProcessor.class);
        when(paymentFactory.create("STRIPE")).thenReturn(processor);
        when(processor.processPayment(any()))
                .thenReturn(new PaymentResult(true, "ch_ghs", "OK",
                        "Stripe"));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            var entity = inv.getArgument(0, OrderEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });

        service.checkout(user, request);

        verify(currencyService).convertFromUsd(anyDouble(), eq(Currency.GHS));

        var orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(Currency.GHS, orderCaptor.getValue().getCurrency());
    }

    @Test
    void checkoutSavesOrderWithCorrectFields() {
        var items = List.of(
                new CartItemDto("p1", "Earbuds", 29.99, 2),
                new CartItemDto("p2", "Case", 9.99, 1)
        );
        var request = makeRequest(items, "CRYPTO", "EXPRESS",
                Currency.USD, List.of(NotificationChannel.EMAIL));

        when(shippingService.calculate("EXPRESS", items, MembershipTier.STANDARD))
                .thenReturn(new ShippingQuoteResponse("EXPRESS", "Express", 15.99,
                        15.99, List.of()));
        when(currencyService.convertFromUsd(anyDouble(), eq(Currency.USD)))
                .thenAnswer(inv -> inv.getArgument(0));

        var processor = mock(PaymentProcessor.class);
        when(paymentFactory.create("CRYPTO")).thenReturn(processor);
        when(processor.processPayment(any()))
                .thenReturn(new PaymentResult(true, "tx_btc123", "Confirmed",
                        "Crypto"));

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            var entity = inv.getArgument(0, OrderEntity.class);
            setId(entity, UUID.randomUUID());
            return entity;
        });

        service.checkout(user, request);

        var captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        var saved = captor.getValue();

        assertTrue(saved.getOrderNumber().startsWith("ORD-"));
        assertEquals(user, saved.getUser());
        assertEquals("CRYPTO", saved.getPaymentMethod());
        assertEquals("tx_btc123", saved.getTransactionId());
        assertEquals("EXPRESS", saved.getShippingMethod());
        assertEquals(Currency.USD, saved.getCurrency());
        assertEquals(2, saved.getItems().size());
    }

    private OrderEntity buildOrderEntity(String orderNumber) {
        var order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setSubtotal(BigDecimal.valueOf(59.98));
        order.setShippingCost(BigDecimal.valueOf(6.49));
        order.setTotal(BigDecimal.valueOf(66.47));
        order.setCurrency(Currency.USD);
        order.setPaymentMethod("STRIPE");
        order.setShippingMethod("STANDARD");
        setId(order, UUID.randomUUID());
        return order;
    }

    @Test
    void findByOrderNumberReturnsOrderWhenExists() {
        var order = buildOrderEntity("ORD-20260331-ABCD");

        when(orderRepository.findByOrderNumber("ORD-20260331-ABCD")).thenReturn(
                Optional.of(order));
        when(notificationLogRepository.findByOrderId(any())).thenReturn(List.of());

        var result = service.findByOrderNumber("ORD-20260331-ABCD");

        assertTrue(result.isPresent());
        assertEquals("ORD-20260331-ABCD", result.get().orderNumber());
    }

    @Test
    void findByOrderNumberReturnsEmptyWhenNotExists() {
        when(orderRepository.findByOrderNumber("ORD-NONEXISTENT")).thenReturn(
                Optional.empty());

        var result = service.findByOrderNumber("ORD-NONEXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserReturnsAllUserOrders() {
        var order1 = buildOrderEntity("ORD-001");
        var order2 = buildOrderEntity("ORD-002");

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(order1, order2));
        when(notificationLogRepository.findByOrderId(any())).thenReturn(List.of());

        var results = service.findByUser(user);

        assertEquals(2, results.size());
    }

    @Test
    void findByUserReturnsEmptyListWhenNoOrders() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of());

        var results = service.findByUser(user);

        assertTrue(results.isEmpty());
    }
}
