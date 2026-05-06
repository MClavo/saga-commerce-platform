package com.mclavo.ecommerce.payment.infrastucture.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.payment.application.PaymentService;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProductReservationSucceededConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "${application.kafka.product-reservation-succeeded-topic}",
            groupId = "${spring.kafka.consumer.group-id:payment-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.payment.infrastucture.messaging.event.ProductReservationSucceededEvent"
            }
    )
    public void consume(@Valid ProductReservationSucceededEvent event) {
        log.info("Consuming product reservation succeeded event for order: {}", event.orderReference());

        try {
            paymentService.processProductReservationSucceeded(event);
        } catch (Exception ex) {
            log.warn("Payment processing failed for order: {}", event.orderReference(), ex);
            paymentEventProducer.publishPaymentFailed(
                    new PaymentFailedEvent(
                            event.orderId(),
                            event.orderReference(),
                            ex.getMessage()));
        }
    }
}
