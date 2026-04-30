package com.mclavo.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.kafka")
public record KafkaOrderProperties(
    String orderConfirmation
) {}
