package com.mclavo.gateway.auth;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
class AuthController {

    @GetMapping("/auth/login")
    Mono<Void> login(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/oauth2/authorization/keycloak"));
        return response.setComplete();
    }

    @GetMapping("/auth/me")
    Mono<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser user)) {
            return Mono.just(Map.of("authenticated", false));
        }

        return Mono.just(Map.of(
                "authenticated", true,
                "username", user.getPreferredUsername(),
                "email", user.getEmail(),
                "name", user.getFullName(),
                "authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()));
    }

    @GetMapping("/auth/csrf")
    Mono<Map<String, String>> csrf(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.<Mono<CsrfToken>>getAttribute(CsrfToken.class.getName()))
                .flatMap(token -> token.map(csrfToken -> Map.of(
                        "headerName", csrfToken.getHeaderName(),
                        "parameterName", csrfToken.getParameterName(),
                        "token", csrfToken.getToken())));
    }
}