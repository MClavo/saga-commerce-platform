package com.mclavo.gateway.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health").permitAll()
                        
                        // Product
                        .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/products", "/api/v1/products/**").hasAuthority("ROLE_ADMIN")
                        
                        // Order
                        .pathMatchers(HttpMethod.POST, "/api/v1/orders", "/api/v1/orders/**").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/**").hasAnyAuthority("ROLE_ORDER_MANAGER", "ROLE_ADMIN")
                        
                        // Customer
                        .pathMatchers("/api/v1/customers", "/api/v1/customers/**").hasAnyAuthority("ROLE_CUSTOMER_SUPPORT", "ROLE_ADMIN")
                        
                        // Payment
                        .pathMatchers("/api/v1/payments", "/api/v1/payments/**").denyAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRealmRolesConverter())))
                .build();

    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> keycloakRealmRolesConverter() {
        return jwt -> Mono.just(new JwtAuthenticationToken(jwt, realmAuthorities(jwt)));
    }

    private Collection<GrantedAuthority> realmAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return List.of();
        }

        Object roles = realmAccess.get("roles");
        if (!(roles instanceof Collection<?> roleNames)) {
            return List.of();
        }

        return roleNames.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

}
