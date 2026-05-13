package com.mclavo.ecommerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/payments", "/api/v1/payments/**").hasAnyAuthority("ROLE_ORDER_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/demo/orders/*/confirm").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/demo/orders/*/fail").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/payments", "/api/v1/payments/**").denyAll()

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
