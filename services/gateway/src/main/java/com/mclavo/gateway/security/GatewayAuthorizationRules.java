package com.mclavo.gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.stereotype.Component;

@Component
class GatewayAuthorizationRules {

    void configure(ServerHttpSecurity.AuthorizeExchangeSpec exchange) {
        exchange
                // Auth
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/auth/login", "/auth/me").permitAll()
                .pathMatchers(HttpMethod.POST, "/auth/logout").authenticated()

                // Product
                .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")

                // Order
                .pathMatchers("/api/v1/orders", "/api/v1/orders/**")
                .hasAnyAuthority("ROLE_ORDER_MANAGER", "ROLE_ADMIN")

                // Customer
                .pathMatchers("/api/v1/customers", "/api/v1/customers/**")
                .hasAnyAuthority("ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN")

                // Payment
                .pathMatchers(HttpMethod.GET, "/api/v1/payments", "/api/v1/payments/**")
                .hasAnyAuthority("ROLE_ORDER_MANAGER", "ROLE_ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/payments/demo/orders/*/confirm").hasAuthority("ROLE_ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/v1/payments/demo/orders/*/fail").hasAuthority("ROLE_ADMIN")
                .pathMatchers("/api/v1/payments", "/api/v1/payments/**").denyAll()

                .anyExchange().authenticated();
    }
}
