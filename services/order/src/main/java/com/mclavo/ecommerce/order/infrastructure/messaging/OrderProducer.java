package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.config.KafkaOrderProperties;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.OrderConfirmation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaOrderProperties kafkaProperties;
    private final KafkaTemplate<String, OrderConfirmation> kafkaTemplate;

    public void publishOrderConfirmation(OrderConfirmation orderConfirmation) {
        log.info("Sending order confirmation for order reference: {}", orderConfirmation.orderReference());
        Message<OrderConfirmation> message = MessageBuilder
                .withPayload(orderConfirmation)
                .setHeader(KafkaHeaders.TOPIC, kafkaProperties.orderTopic())
                .build();

        kafkaTemplate.send(message);
    }
}