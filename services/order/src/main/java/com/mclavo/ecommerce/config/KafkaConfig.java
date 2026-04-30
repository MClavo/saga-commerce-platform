package com.mclavo.ecommerce.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaOrderProperties.class)
public class KafkaConfig {
    @Bean
    NewTopic orderConfirmationTopic(KafkaOrderProperties properties) {
        return TopicBuilder.name(properties.orderConfirmation())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
