package com.mclavo.ecommerce.order.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.order.application.OrderService;
import com.mclavo.ecommerce.order.infrastructure.messaging.event.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentFailedConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${application.kafka.payment-failed-topic}")
    public void consume(PaymentFailedEvent event) {
        log.info("Received payment failed event for order reference: {}", event.orderReference());
        orderService.failPayment(event);
    }
}
