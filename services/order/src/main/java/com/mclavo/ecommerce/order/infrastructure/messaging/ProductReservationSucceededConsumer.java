package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.order.application.OrderService;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.ProductReservationSucceededEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReservationSucceededConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${application.kafka.product-reservation-succeeded-topic}")
    public void consume(ProductReservationSucceededEvent event) {
        log.info("Received product reservation succeeded event for order reference: {}", event.orderReference());
        orderService.reserveProducts(event);
    }
}
