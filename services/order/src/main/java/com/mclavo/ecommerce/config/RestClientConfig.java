package com.mclavo.ecommerce.config;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.mclavo.ecommerce.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@Slf4j
public class RestClientConfig {

    @Bean
    @Qualifier("productRestClient")
    RestClient productRestClient(RestClient.Builder builder, ClientProperties properties) {
        return builder
                .baseUrl(properties.productUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    log.error(
                            "Product client service error. Status: {}, Body: {}",
                            response.getStatusCode(),
                            body);

                    throw new BusinessException(
                            "Product service error. Status: " + response.getStatusCode()
                                    + ", body: " + body);
                })
                .build();
    }

}
