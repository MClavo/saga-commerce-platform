package com.mclavo.ecommerce.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.mclavo.ecommerce.config.KafkaPaymentProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaPaymentProperties properties;
    private final KafkaTemplate<String, PaymentNotificationRequest> kafkaTemplate;

    public void sendPaymentNotification(PaymentNotificationRequest request) {
        log.info("Sending payment notification for order: {}", request.orderReference());
        
        /* Message<PaymentNotificationRequest> message = MessageBuilder
            .withPayload(request)
            .setHeader(KafkaHeaders.TOPIC, properties.paymentTopic())
            .build();
        
        kafkaTemplate.send(message); */

        kafkaTemplate.send(properties.paymentTopic(), request);
    }
}
