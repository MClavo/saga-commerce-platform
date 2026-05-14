package com.mclavo.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

import com.mclavo.gateway.auth.KeycloakLogoutSuccessHandler;

/**
 * Security configuration for the API Gateway.
 *
 * <p/>
 * This class sets up the security filter chain, including CSRF protection,
 * authentication, and authorization rules.
 * <ul>
 * <li>{@link GatewayAuthorizationRules} is used to define the authorization
 * rules for different endpoints.</li>
 * <li>{@link KeycloakRealmRoleConverter} is used to convert JWT tokens from
 * Keycloak into Spring Security authorities.</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    private final GatewayAuthorizationRules authorizationRules;
    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;
    private final KeycloakLogoutSuccessHandler keycloakLogoutSuccessHandler;

    SecurityConfig(
            GatewayAuthorizationRules authorizationRules,
            KeycloakRealmRoleConverter keycloakRealmRoleConverter,
            KeycloakLogoutSuccessHandler keycloakLogoutSuccessHandler) {
        this.authorizationRules = authorizationRules;
        this.keycloakRealmRoleConverter = keycloakRealmRoleConverter;
        this.keycloakLogoutSuccessHandler = keycloakLogoutSuccessHandler;
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeExchange(authorizationRules::configure)
                .oauth2Login(oauth2 -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRealmRoleConverter)))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(keycloakLogoutSuccessHandler))
                .build();
    }
}