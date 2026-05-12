package com.mclavo.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.kafka")
public record KafkaOrderProperties(
        String productReservationRequestedTopic,
        String productReservationSucceededTopic,
        String productReservationFailedTopic,
        String paymentRequestedTopic,
        String paymentConfirmedTopic,
        String paymentFailedTopic,
        String orderConfirmedTopic,
        String orderCancelledTopic,
        String notificationRequestedTopic
) {}
