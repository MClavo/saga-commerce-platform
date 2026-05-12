package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.order.application.OrderService;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentConfirmedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentConfirmedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${application.kafka.payment-confirmed-topic}")
    public void consume(PaymentConfirmedEvent event) {
        log.info("Received payment confirmed event for order reference: {}", event.orderReference());
        orderService.confirmOrder(event);
    }
}
