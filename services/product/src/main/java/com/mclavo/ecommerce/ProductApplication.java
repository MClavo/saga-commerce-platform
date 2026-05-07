package com.mclavo.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mclavo.ecommerce.config.KafkaProductProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaProductProperties.class)
public class ProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}

}
