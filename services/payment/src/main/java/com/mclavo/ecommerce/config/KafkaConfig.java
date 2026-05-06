package com.mclavo.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaPaymentProperties.class)
public class KafkaConfig {
    @Bean
    NewTopic paymentTopic(KafkaPaymentProperties properties) {
        return createTopic(properties.paymentTopic());
    }

    @Bean
    NewTopic orderCreatedTopic(KafkaPaymentProperties properties) {
        return createTopic(properties.orderCreatedTopic());
    }

    @Bean
    NewTopic paymentProcessedTopic(KafkaPaymentProperties properties) {
        return createTopic(properties.paymentProcessedTopic());
    }

    @Bean
    NewTopic paymentFailedTopic(KafkaPaymentProperties properties) {
        return createTopic(properties.paymentFailedTopic());
    }

    private NewTopic createTopic(String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
