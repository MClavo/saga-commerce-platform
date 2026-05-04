package com.mclavo.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestInterceptor;




/**
 * FeignSecurityConfig provides a RequestInterceptor bean that propagates the
 * current JWT from the Spring Security context to outgoing Feign client
 * requests. When the current Authentication is a JwtAuthenticationToken the
 * interceptor will add an HTTP Authorization header with the Bearer token
 * value so downstream services receive the original JWT.
 */
@Configuration
public class FeignSecurityConfig {

    /**
     * Create a RequestInterceptor that adds an Authorization header containing
     * the Bearer JWT when the current security context holds a
     * JwtAuthenticationToken.
     *
     * @return a RequestInterceptor that propagates the JWT as
     *         Authorization: Bearer &lt;token&gt;
     */
    @Bean
    RequestInterceptor bearerTokenRequestInterceptor() {
        return template -> {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                template.header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtAuth.getToken().getTokenValue());
            }

        };
    }
}
