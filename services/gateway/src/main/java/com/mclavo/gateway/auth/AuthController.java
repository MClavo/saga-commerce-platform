package com.mclavo.gateway.auth;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles the login endpoint and redirects the user to the OAuth2 provider.
     *
     * @param response the server HTTP response
     * @return a Mono completing the response
     */
    @GetMapping("/login")
    Mono<Void> login(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create("/oauth2/authorization/keycloak"));

        return response.setComplete();
    }

    /**
     * Handles the user info endpoint and returns the authenticated user's information.
     *
     * @param authentication the authentication object
     * @return a Mono containing the authenticated user's information
     */
    @GetMapping("/me")
    Mono<AuthMeResponse> me(Authentication authentication) {
        return Mono.just(authService.authenticate(authentication));
    }
}