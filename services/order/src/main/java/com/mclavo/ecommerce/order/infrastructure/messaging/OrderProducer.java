package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.config.KafkaOrderProperties;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.NotificationRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderCancelledEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentRequestedEvent;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaOrderProperties kafkaProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductReservationRequested(ProductReservationRequestedEvent event) {
        log.info("Publishing product reservation requested event for order reference: {}", event.orderReference());
        kafkaTemplate.send(kafkaProperties.productReservationRequestedTopic(), event.orderReference(), event);
    }

    public void publishPaymentRequested(PaymentRequestedEvent event) {
        log.info("Publishing payment requested event for order reference: {}", event.orderReference());
        kafkaTemplate.send(kafkaProperties.paymentRequestedTopic(), event.orderReference(), event);
    }

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Publishing order confirmed event for order reference: {}", event.orderReference());
        kafkaTemplate.send(kafkaProperties.orderConfirmedTopic(), event.orderReference(), event);
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing order cancelled event for order reference: {}", event.orderReference());
        kafkaTemplate.send(kafkaProperties.orderCancelledTopic(), event.orderReference(), event);
    }

    public void publishNotificationRequested(NotificationRequestedEvent event) {
        log.info("Publishing notification requested event for order reference: {}", event.orderReference());
        kafkaTemplate.send(kafkaProperties.notificationRequestedTopic(), event.orderReference(), event);
    }
}
