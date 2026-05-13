package com.mclavo.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.mclavo.ecommerce.config.KafkaOrderProperties;
import com.mclavo.ecommerce.customer.CustomerClient;
import com.mclavo.ecommerce.customer.CustomerResponse;
import com.mclavo.ecommerce.exception.DuplicateOrderReferenceException;
import com.mclavo.ecommerce.order.api.OrderProductRequest;
import com.mclavo.ecommerce.order.api.OrderRequest;
import com.mclavo.ecommerce.order.application.OrderMapper;
import com.mclavo.ecommerce.order.application.OrderService;
import com.mclavo.ecommerce.order.domain.Order;
import com.mclavo.ecommerce.order.domain.OrderProductSnapshot;
import com.mclavo.ecommerce.order.domain.OrderStatus;
import com.mclavo.ecommerce.order.domain.PaymentMethod;
import com.mclavo.ecommerce.order.infrastructure.messaging.OrderProducer;
import com.mclavo.ecommerce.order.infrastructure.messaging.PaymentConfirmedConsumer;
import com.mclavo.ecommerce.order.infrastructure.messaging.PaymentFailedConsumer;
import com.mclavo.ecommerce.order.infrastructure.messaging.ProductReservationFailedConsumer;
import com.mclavo.ecommerce.order.infrastructure.messaging.ProductReservationSucceededConsumer;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.NotificationRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.NotificationType;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentConfirmedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentFailedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationFailedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationFailedPayload;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationItem;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderLineRepository;
import com.mclavo.ecommerce.order.infrastructure.persistence.OrderRepository;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        ProductReservationSucceededConsumer.class,
        ProductReservationFailedConsumer.class,
        PaymentConfirmedConsumer.class,
        PaymentFailedConsumer.class,
        OrderService.class,
        OrderMapper.class,
        OrderProducer.class,
        OrderSagaSmokeTest.TestConfig.class
})
class OrderSagaSmokeTest {

    @Resource
    private OrderService orderService;

    @Resource
    private ProductReservationSucceededConsumer productReservationSucceededConsumer;

    @Resource
    private ProductReservationFailedConsumer productReservationFailedConsumer;

    @Resource
    private PaymentConfirmedConsumer paymentConfirmedConsumer;

    @Resource
    private PaymentFailedConsumer paymentFailedConsumer;

    @Resource
    private CustomerClient customerClient;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private OrderLineRepository orderLineRepository;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        reset(customerClient, orderRepository, orderLineRepository, kafkaTemplate);
    }

    @Test
    void should_Create_Pending_Order_And_Publish_ProductReservationRequestedEvent() {
        var request = orderRequest(List.of(
                new OrderProductRequest(5, 1),
                new OrderProductRequest(5, 1)));

        when(orderRepository.findByReference("ORD-42")).thenReturn(Optional.empty());
        when(customerClient.findCustomerById("customer-1")).thenReturn(Optional.of(customer()));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 42);
            return order;
        });

        var response = orderService.createOrder(request);

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertAll(
                () -> assertEquals(42, response.orderId()),
                () -> assertEquals(OrderStatus.PRODUCT_RESERVATION_PENDING, response.status()),
                () -> assertEquals(OrderStatus.PRODUCT_RESERVATION_PENDING, savedOrder.getStatus()),
                () -> assertEquals(BigDecimal.ZERO, savedOrder.getTotalAmount()),
                () -> assertEquals("Ada", savedOrder.getCustomerFirstName()),
                () -> assertEquals(5, savedOrder.getOrderLines().getFirst().getProductId()),
                () -> assertEquals(2, savedOrder.getOrderLines().getFirst().getQuantity()));

        var eventCaptor = ArgumentCaptor.forClass(ProductReservationRequestedEvent.class);
        verify(kafkaTemplate).send(eq("product.reservation.requested"), eq("ORD-42"), eventCaptor.capture());
        assertAll(
                () -> assertEquals(42, eventCaptor.getValue().orderId()),
                () -> assertEquals("ORD-42", eventCaptor.getValue().orderReference()),
                () -> assertEquals(5, eventCaptor.getValue().products().getFirst().productId()),
                () -> assertEquals(2, eventCaptor.getValue().products().getFirst().quantity()));
    }

    @Test
    void should_Return_Existing_Order_When_Duplicate_Create_Request_Matches() {
        Order existingOrder = savedRequestedOrder();
        when(orderRepository.findByReference("ORD-42")).thenReturn(Optional.of(existingOrder));

        var response = orderService.createOrder(orderRequest(List.of(new OrderProductRequest(5, 2))));

        assertAll(
                () -> assertEquals(42, response.orderId()),
                () -> assertEquals(OrderStatus.PRODUCT_RESERVATION_PENDING, response.status()));
        verifyNoInteractions(customerClient, kafkaTemplate);
    }

    @Test
    void should_Reject_Duplicate_Order_Reference_With_Different_Request() {
        Order existingOrder = savedRequestedOrder();
        when(orderRepository.findByReference("ORD-42")).thenReturn(Optional.of(existingOrder));

        var request = orderRequest(List.of(new OrderProductRequest(6, 2)));

        assertThrows(DuplicateOrderReferenceException.class, () -> orderService.createOrder(request));
        verifyNoInteractions(customerClient, kafkaTemplate);
    }

    @Test
    void should_Store_Product_Snapshots_And_Publish_PaymentRequested_When_Reservation_Succeeds() {
        Order order = savedRequestedOrder();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));

        productReservationSucceededConsumer.consume(new ProductReservationSucceededEvent(
                42,
                "ORD-42",
                List.of(new ProductReservationItem(5, "Claw Hammer", 2, new BigDecimal("18.99")))));

        assertAll(
                () -> assertEquals(OrderStatus.AWAITING_PAYMENT, order.getStatus()),
                () -> assertEquals(new BigDecimal("37.98"), order.getTotalAmount()),
                () -> assertEquals("Claw Hammer", order.getOrderLines().getFirst().getProductName()));

        var eventCaptor = ArgumentCaptor.forClass(PaymentRequestedEvent.class);
        verify(kafkaTemplate).send(eq("payment.requested"), eq("ORD-42"), eventCaptor.capture());
        assertAll(
                () -> assertEquals(42, eventCaptor.getValue().orderId()),
                () -> assertEquals(new BigDecimal("37.98"), eventCaptor.getValue().totalAmount()),
                () -> assertEquals(PaymentMethod.CREDIT_CARD, eventCaptor.getValue().paymentMethod()));
    }

    @Test
    void should_Mark_ProductReservationFailed_And_Request_Notification_When_Reservation_Fails() {
        Order order = savedRequestedOrder();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));

        productReservationFailedConsumer.consume(new ProductReservationFailedEvent(42, "ORD-42", "not enough stock"));

        assertEquals(OrderStatus.PRODUCT_RESERVATION_FAILED, order.getStatus());
        verify(kafkaTemplate, never()).send(eq("order.cancelled"), any(), any());

        var notificationCaptor = ArgumentCaptor.forClass(NotificationRequestedEvent.class);
        verify(kafkaTemplate).send(eq("notification.requested"), eq("ORD-42"), notificationCaptor.capture());
        NotificationRequestedEvent notification = notificationCaptor.getValue();
        ProductReservationFailedPayload payload = (ProductReservationFailedPayload) notification.payload();

        assertAll(
                () -> assertEquals(NotificationType.PRODUCT_RESERVATION_FAILED, notification.notificationType()),
                () -> assertEquals("not enough stock", payload.failureReason()));
    }

    @Test
    void should_Confirm_Order_And_Request_Notification_When_Payment_Confirmed() {
        Order order = savedReservedOrder();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));

        paymentConfirmedConsumer.consume(new PaymentConfirmedEvent(42, "ORD-42", "PAY-42"));

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        var orderConfirmedCaptor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(kafkaTemplate).send(eq("order.confirmed"), eq("ORD-42"), orderConfirmedCaptor.capture());
        assertEquals(42, orderConfirmedCaptor.getValue().orderId());

        var notificationCaptor = ArgumentCaptor.forClass(NotificationRequestedEvent.class);
        verify(kafkaTemplate).send(eq("notification.requested"), eq("ORD-42"), notificationCaptor.capture());
        NotificationRequestedEvent notification = notificationCaptor.getValue();
        OrderConfirmedPayload payload = (OrderConfirmedPayload) notification.payload();

        assertAll(
                () -> assertEquals(NotificationType.ORDER_CONFIRMED, notification.notificationType()),
                () -> assertEquals("ada@example.com", notification.recipient().email()),
                () -> assertEquals("PAY-42", payload.paymentReference()),
                () -> assertEquals("Claw Hammer", payload.products().getFirst().productName()));
    }

    @Test
    void should_Mark_PaymentFailed_Publish_OrderCancelled_And_Request_Notification_When_Payment_Fails() {
        Order order = savedReservedOrder();
        when(orderRepository.findByIdForUpdate(42)).thenReturn(Optional.of(order));

        paymentFailedConsumer.consume(new PaymentFailedEvent(42, "ORD-42", "card declined"));

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        verify(kafkaTemplate).send(eq("order.cancelled"), eq("ORD-42"), any());

        var notificationCaptor = ArgumentCaptor.forClass(NotificationRequestedEvent.class);
        verify(kafkaTemplate).send(eq("notification.requested"), eq("ORD-42"), notificationCaptor.capture());
        NotificationRequestedEvent notification = notificationCaptor.getValue();
        PaymentFailedPayload payload = (PaymentFailedPayload) notification.payload();

        assertAll(
                () -> assertEquals(NotificationType.PAYMENT_FAILED, notification.notificationType()),
                () -> assertEquals("card declined", payload.failureReason()),
                () -> assertEquals(new BigDecimal("37.98"), payload.totalAmount()));
    }

    private OrderRequest orderRequest(List<OrderProductRequest> products) {
        return new OrderRequest(
                null,
                "ORD-42",
                PaymentMethod.CREDIT_CARD,
                "customer-1",
                products);
    }

    private Order savedRequestedOrder() {
        Order order = new Order(
                "ORD-42",
                "customer-1",
                PaymentMethod.CREDIT_CARD,
                "Ada",
                "Lovelace",
                "ada@example.com");
        order.addRequestedLine(5, 2);
        ReflectionTestUtils.setField(order, "id", 42);
        return order;
    }

    private Order savedReservedOrder() {
        Order order = savedRequestedOrder();
        order.applyProductSnapshots(List.of(
                new OrderProductSnapshot(5, "Claw Hammer", 2, new BigDecimal("18.99"))));
        order.markProductsReserved();
        return order;
    }

    private CustomerResponse customer() {
        return new CustomerResponse("customer-1", "Ada", "Lovelace", "ada@example.com");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        KafkaOrderProperties kafkaOrderProperties() {
            return new KafkaOrderProperties(
                    "product.reservation.requested",
                    "product.reservation.succeeded",
                    "product.reservation.failed",
                    "payment.requested",
                    "payment.confirmed",
                    "payment.failed",
                    "order.confirmed",
                    "order.cancelled",
                    "notification.requested");
        }

        @Bean
        CustomerClient customerClient() {
            return mock(CustomerClient.class);
        }

        @Bean
        OrderRepository orderRepository() {
            return mock(OrderRepository.class);
        }

        @Bean
        OrderLineRepository orderLineRepository() {
            return mock(OrderLineRepository.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }
}
