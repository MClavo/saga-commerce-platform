package com.mclavo.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCreatedEvent;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.ProductReservationFailedEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final ProductService productService;
    private final ProductReservationEventProducer producer;

    @KafkaListener(
            topics = "${application.kafka.order-created-topic}",
            groupId = "${spring.kafka.consumer.group-id:product-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCreatedEvent"
            }
    )
    public void consume(@Valid OrderCreatedEvent event) {
        log.info("Consuming order created event for stock reservation: {}", event.orderReference());

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
