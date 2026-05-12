package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.order.application.OrderService;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReservationFailedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${application.kafka.product-reservation-failed-topic}")
    public void consume(ProductReservationFailedEvent event) {
        log.info("Received product reservation failed event for order reference: {}", event.orderReference());
        orderService.failProductReservation(event);
    }
}
