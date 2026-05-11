package com.mclavo.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationFailedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationRequestedEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class ProductReservationRequestedConsumer {

    private final ProductService productService;
    private final ProductReservationEventProducer producer;

    @KafkaListener(
            topics = "${application.kafka.product-reservation-requested-topic}",
            groupId = "${spring.kafka.consumer.group-id:product-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationRequestedEvent"
            }
    )
    public void consume(@Valid ProductReservationRequestedEvent event) {
        log.info("Consuming product reservation requested event for order: {}", event.orderReference());

        try {
            producer.publishReservationSucceeded(productService.reserveStock(event));
        } catch (Exception ex) {
            log.warn("Stock reservation failed for order: {}", event.orderReference(), ex);
            producer.publishReservationFailed(
                    new ProductReservationFailedEvent(
                            event.orderId(),
                            event.orderReference(),
                            ex.getMessage()));
        }
    }
}
