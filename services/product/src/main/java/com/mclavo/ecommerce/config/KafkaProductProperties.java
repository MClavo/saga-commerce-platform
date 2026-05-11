package com.mclavo.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.kafka")
public record KafkaProductProperties(
        String productReservationRequestedTopic,
        String productReservationSucceededTopic,
        String productReservationFailedTopic,
        String orderConfirmedTopic,
        String orderCancelledTopic
) {
}
