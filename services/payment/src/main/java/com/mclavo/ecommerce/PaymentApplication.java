package com.mclavo.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.mclavo.ecommerce.config.KafkaPaymentProperties;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(KafkaPaymentProperties.class)
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

}
