package com.mclavo.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import com.mclavo.ecommerce.config.KafkaProductProperties;
import com.mclavo.ecommerce.exception.ProductPurchaseException;
import com.mclavo.ecommerce.product.application.ProductMapper;
import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.domain.Product;
import com.mclavo.ecommerce.product.domain.ProductReservation;
import com.mclavo.ecommerce.product.domain.ProductReservationStatus;
import com.mclavo.ecommerce.product.infrastructure.messaging.OrderCancelledConsumer;
import com.mclavo.ecommerce.product.infrastructure.messaging.OrderConfirmedConsumer;
import com.mclavo.ecommerce.product.infrastructure.messaging.ProductReservationEventProducer;
import com.mclavo.ecommerce.product.infrastructure.messaging.ProductReservationRequestedConsumer;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCancelledEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderConfirmedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderProductItem;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationFailedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationRequestedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationSucceededEvent;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductRepository;
import com.mclavo.ecommerce.product.infrastructure.persistence.ProductReservationRepository;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        ProductReservationRequestedConsumer.class,
        OrderCancelledConsumer.class,
        OrderConfirmedConsumer.class,
        ProductService.class,
        ProductMapper.class,
        ProductReservationEventProducer.class,
        ProductSagaSmokeTest.TestConfig.class
})
class ProductSagaSmokeTest {

    @Resource
    private ProductReservationRequestedConsumer productReservationRequestedConsumer;

    @Resource
    private OrderCancelledConsumer orderCancelledConsumer;

    @Resource
    private OrderConfirmedConsumer orderConfirmedConsumer;

    @Resource
    private ProductService productService;

    @Resource
    private ProductRepository productRepository;

    @Resource
    private ProductReservationRepository reservationRepository;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        reset(productRepository, reservationRepository, kafkaTemplate);
    }

    @Test
    void should_Reserve_Stock_And_PublishEvent_when_ProductReservationRequestedEvent_Consumed() {

        // given
        var product = Product.builder()
                .id(5)
                .name("Claw Hammer")
                .availableQuantity(10)
                .reservedQuantity(0)
                .price(new BigDecimal("18.99"))
                .build();

        var event = new ProductReservationRequestedEvent(
                42,
                "ORD-42",
                List.of(new OrderProductItem(5, 3)));

        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of());
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));
        when(reservationRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        productReservationRequestedConsumer.consume(event);

        // then
        assertEquals(7, product.getAvailableQuantity());
        assertEquals(3, product.getReservedQuantity());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<ProductReservation>> reservationCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(reservationRepository).saveAll(reservationCaptor.capture());
        ProductReservation reservation = reservationCaptor.getValue().iterator().next();
        assertEquals(42, reservation.getOrderId());
        assertEquals("ORD-42", reservation.getOrderReference());
        assertEquals(5, reservation.getProductId());
        assertEquals(3, reservation.getQuantity());
        assertEquals(ProductReservationStatus.RESERVED, reservation.getStatus());

        var eventCaptor = ArgumentCaptor.forClass(ProductReservationSucceededEvent.class);
        verify(kafkaTemplate).send(eq("product.reservation.succeeded"), eq("ORD-42"), eventCaptor.capture());
        assertEquals(42, eventCaptor.getValue().orderId());
        assertEquals("ORD-42", eventCaptor.getValue().orderReference());
        assertEquals(1, eventCaptor.getValue().products().size());
        assertEquals(5, eventCaptor.getValue().products().get(0).productId());
        assertEquals("Claw Hammer", eventCaptor.getValue().products().get(0).productName());
        assertEquals(3, eventCaptor.getValue().products().get(0).quantity());
        assertEquals(new BigDecimal("18.99"), eventCaptor.getValue().products().get(0).unitPrice());
    }

    @Test
    void should_Publish_FailureEvent_when_Stock_Cannot_Be_Reserved() {

        // given
        var product = product(5, 5, 0);
        var event = productReservationRequestedEvent(42, "ORD-42", 5, 7);

        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of());
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));

        // when
        productReservationRequestedConsumer.consume(event);

        // then
        assertAll(
                () -> assertEquals(5, product.getAvailableQuantity()),
                () -> assertEquals(0, product.getReservedQuantity()));
        verify(reservationRepository, never()).saveAll(any());

        var eventCaptor = ArgumentCaptor.forClass(ProductReservationFailedEvent.class);
        verify(kafkaTemplate).send(eq("product.reservation.failed"), eq("ORD-42"), eventCaptor.capture());
        assertEquals(42, eventCaptor.getValue().orderId());
        assertEquals("ORD-42", eventCaptor.getValue().orderReference());
    }

    @Test
    void should_Not_Reserve_Stock_Again_when_Order_Already_Has_Reservation() {

        // given
        var product = product(5, 10, 0);
        var reservation = reservation(42, "ORD-42", 5, 3);
        var event = productReservationRequestedEvent(42, "ORD-42", 5, 3);
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of(reservation));
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));

        // when
        var result = productService.reserveStock(event);

        // then
        assertEquals(42, result.orderId());
        assertEquals("ORD-42", result.orderReference());
        assertEquals(3, result.products().get(0).quantity());
        verify(reservationRepository, never()).saveAll(any());
    }

    @Test
    void should_Throw_when_Reserved_Product_Is_Not_Found() {

        // given
        var event = productReservationRequestedEvent(42, "ORD-42", 5, 3);
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of());
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of());

        // when / then
        assertThrows(ProductPurchaseException.class, () -> productService.reserveStock(event));
        verify(reservationRepository, never()).saveAll(any());
    }

    @Test
    void should_Commit_Reserved_Stock_when_Order_Is_Confirmed() {

        // given
        var product = product(5, 3, 7);
        var reservation = reservation(42, "ORD-42", 5, 7);
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of(reservation));
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));

        // when
        orderConfirmedConsumer.consume(new OrderConfirmedEvent(42, "ORD-42"));

        // then
        assertAll(
                () -> assertEquals(3, product.getAvailableQuantity()),
                () -> assertEquals(0, product.getReservedQuantity()),
                () -> assertEquals(ProductReservationStatus.COMMITTED, reservation.getStatus()));
    }

    @Test
    void should_Release_Reserved_Stock_when_Order_Is_Cancelled() {

        // given
        var product = product(5, 3, 7);
        var reservation = reservation(42, "ORD-42", 5, 7);
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of(reservation));
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));

        // when
        orderCancelledConsumer.consume(new OrderCancelledEvent(42, "ORD-42"));

        // then
        assertAll(
                () -> assertEquals(10, product.getAvailableQuantity()),
                () -> assertEquals(0, product.getReservedQuantity()),
                () -> assertEquals(ProductReservationStatus.RELEASED, reservation.getStatus()));
    }

    @Test
    void should_Ignore_Finalization_when_Order_Has_No_Reserved_Reservations() {

        // given
        var reservation = ProductReservation.builder()
                .orderId(42)
                .orderReference("ORD-42")
                .productId(5)
                .quantity(7)
                .status(ProductReservationStatus.COMMITTED)
                .build();
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of(reservation));

        // when
        orderCancelledConsumer.consume(new OrderCancelledEvent(42, "ORD-42"));
        // productService.releaseReservedStock(new OrderCancelledEvent(42, "ORD-42"));

        // then
        verify(productRepository, never()).findAllByIdInOrderByIdForUpdate(any());
        assertEquals(ProductReservationStatus.COMMITTED, reservation.getStatus());
    }

    @Test
    void should_Throw_when_Committed_Quantity_Exceeds_Reserved_Stock() {

        // given
        var product = product(5, 7, 3);
        var reservation = reservation(42, "ORD-42", 5, 7);
        when(reservationRepository.findByOrderIdOrderByProductId(42)).thenReturn(List.of(reservation));
        when(productRepository.findAllByIdInOrderByIdForUpdate(List.of(5))).thenReturn(List.of(product));

        // when / then
        assertThrows(ProductPurchaseException.class,
                () -> orderConfirmedConsumer.consume(
                        new OrderConfirmedEvent(42, "ORD-42")));
        assertEquals(ProductReservationStatus.RESERVED, reservation.getStatus());
    }

    private Product product(Integer id, Integer availableQuantity, Integer reservedQuantity) {
        return Product.builder()
                .id(id)
                .name("Claw Hammer")
                .availableQuantity(availableQuantity)
                .reservedQuantity(reservedQuantity)
                .price(new BigDecimal("18.99"))
                .build();
    }

    private ProductReservation reservation(
            Integer orderId,
            String orderReference,
            Integer productId,
            Integer quantity) {

        return ProductReservation.builder()
                .orderId(orderId)
                .orderReference(orderReference)
                .productId(productId)
                .quantity(quantity)
                .status(ProductReservationStatus.RESERVED)
                .build();
    }

    private ProductReservationRequestedEvent productReservationRequestedEvent(
            Integer orderId,
            String orderReference,
            Integer productId,
            Integer quantity) {

        return new ProductReservationRequestedEvent(
                orderId,
                orderReference,
                List.of(new OrderProductItem(productId, quantity)));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        KafkaProductProperties kafkaProductProperties() {
            return new KafkaProductProperties(
                    "product.reservation.requested",
                    "product.reservation.succeeded",
                    "product.reservation.failed",
                    "order.confirmed",
                    "order.cancelled");
        }

        @Bean
        ProductRepository productRepository() {
            return mock(ProductRepository.class);
        }

        @Bean
        ProductReservationRepository productReservationRepository() {
            return mock(ProductReservationRepository.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> kafkaTemplate() {
            return mock(KafkaTemplate.class);
        }
    }
}
