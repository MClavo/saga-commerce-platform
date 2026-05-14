package com.mclavo.gateway.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class KeycloakLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    private static final String KEYCLOAK_LOGOUT_ENDPOINT =
            "http://localhost:9098/realms/ecommerce/protocol/openid-connect/logout";

    private static final String POST_LOGOUT_REDIRECT_URI =
            "http://localhost:8222";

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();

        String logoutUrl = buildLogoutUrl(authentication);

        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(logoutUrl));

        return exchange.getResponse().setComplete();
    }

    private String buildLogoutUrl(Authentication authentication) {
        String encodedRedirectUri = URLEncoder.encode(POST_LOGOUT_REDIRECT_URI, StandardCharsets.UTF_8);

        if (authentication instanceof OAuth2AuthenticationToken
                && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            String idToken = oidcUser.getIdToken().getTokenValue();
            String encodedIdToken = URLEncoder.encode(idToken, StandardCharsets.UTF_8);

            return KEYCLOAK_LOGOUT_ENDPOINT
                    + "?id_token_hint=" + encodedIdToken
                    + "&post_logout_redirect_uri=" + encodedRedirectUri;
        }

        return KEYCLOAK_LOGOUT_ENDPOINT
                + "?post_logout_redirect_uri=" + encodedRedirectUri;
    }
}