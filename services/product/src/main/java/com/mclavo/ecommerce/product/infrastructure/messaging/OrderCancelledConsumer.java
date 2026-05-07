package com.mclavo.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCancelledEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledConsumer {

    private final ProductService productService;

    @KafkaListener(
            topics = "${application.kafka.order-cancelled-topic}",
            groupId = "${spring.kafka.consumer.group-id:product-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderCancelledEvent"
            }
    )
    public void consume(@Valid OrderCancelledEvent event) {
        log.info("Consuming order cancelled event for stock release: {}", event.orderReference());
        productService.releaseReservedStock(event);
    }
}
