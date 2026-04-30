package com.mclavo.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.client")
public record ClientProperties (
    String productUrl,
    String customerUrl,
    String paymentUrl
){ }
