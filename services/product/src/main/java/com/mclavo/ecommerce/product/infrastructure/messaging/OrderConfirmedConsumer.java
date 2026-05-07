package com.mclavo.ecommerce.product.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mclavo.ecommerce.product.application.ProductService;
import com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderConfirmedEvent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmedConsumer {

    private final ProductService productService;

    @KafkaListener(
            topics = "${application.kafka.order-confirmed-topic}",
            groupId = "${spring.kafka.consumer.group-id:product-service}",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.mclavo.ecommerce.product.infrastructure.messaging.event.OrderConfirmedEvent"
            }
    )
    public void consume(@Valid OrderConfirmedEvent event) {
        log.info("Consuming order confirmed event for stock commit: {}", event.orderReference());
        productService.commitReservedStock(event);
    }
}
