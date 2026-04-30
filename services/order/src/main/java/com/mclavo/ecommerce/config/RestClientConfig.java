package com.mclavo.ecommerce.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.mclavo.ecommerce.exception.BusinessException;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

    @Bean
    @Qualifier("productRestClient")
    RestClient productRestClient(RestClient.Builder builder, ClientProperties properties) {
        return builder
            .baseUrl(properties.productUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                throw new BusinessException(
                    "Product service error. Status: " + response.getStatusCode()
                );
            })
            .build();
    }

}
