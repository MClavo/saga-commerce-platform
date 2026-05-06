package com.mclavo.ecommerce.payment.infrastucture.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.payment.application.PaymentService;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.OrderCreatedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentFailedEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(
            topics = "${application.kafka.order-created-topic}",
            groupId = "${spring.kafka.consumer.group-id:payment-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.payment.infrastucture.messaging.event.OrderCreatedEvent"
            }
    )
    public void consume(@Valid OrderCreatedEvent event) {
        log.info("Consuming order created event: {}", event.orderReference());

        try {
            paymentService.processOrderCreated(event);
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
