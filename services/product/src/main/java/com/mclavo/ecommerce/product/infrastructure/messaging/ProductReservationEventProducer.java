package com.mclavo.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.config.KafkaProductProperties;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationFailedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationSucceededEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReservationEventProducer {

    private final KafkaProductProperties properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationSucceeded(ProductReservationSucceededEvent event) {
        log.info("Publishing product reservation succeeded event for order: {}", event.orderReference());
        kafkaTemplate.send(properties.productReservationSucceededTopic(), event.orderReference(), event);
    }

    public void publishReservationFailed(ProductReservationFailedEvent event) {
        log.info("Publishing product reservation failed event for order: {}", event.orderReference());
        kafkaTemplate.send(properties.productReservationFailedTopic(), event.orderReference(), event);
    }
}
