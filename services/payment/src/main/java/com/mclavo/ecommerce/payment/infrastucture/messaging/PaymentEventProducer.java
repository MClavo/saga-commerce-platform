package com.mclavo.ecommerce.payment.infrastucture.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.config.KafkaPaymentProperties;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentFailedEvent;
import com.mclavo.ecommerce.payment.infrastucture.messaging.event.PaymentProcessedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaPaymentProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Publishing payment processed event for order: {}", event.orderReference());
        kafkaTemplate.send(properties.paymentProcessedTopic(), event.orderReference(), event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("Publishing payment failed event for order: {}", event.orderReference());
        kafkaTemplate.send(properties.paymentFailedTopic(), event.orderReference(), event);
    }
}
