package com.mclavo.gateway.security;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

/**
 * Security configuration for the API Gateway.
 *
 * <p/>
 * This class sets up the BFF security filter chain, including OAuth2 Login,
 * OAuth2 Client support for TokenRelay, session logout, and authorization
 * rules.
 * <ul>
 * <li>{@link GatewayAuthorizationRules} is used to define the authorization
 * rules for different endpoints.</li>
 * </ul>
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

	private final GatewayAuthorizationRules authorizationRules;
	private final String frontendUrl;

	SecurityConfig(
			GatewayAuthorizationRules authorizationRules,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.authorizationRules = authorizationRules;
		this.frontendUrl = frontendUrl;
	}

	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.cors(Customizer.withDefaults())
				
				// Disable CSRF since we're using cookies for session tracking and not exposing a public API
				.csrf(ServerHttpSecurity.CsrfSpec::disable)

				// URL authorization
				.authorizeExchange(authorizationRules::configure)

				// OAuth2 Login with custom success handler to redirect to frontend
				.oauth2Login(oauth2 -> oauth2
						.authenticationSuccessHandler((webFilterExchange, authentication) -> {
							var response = webFilterExchange.getExchange().getResponse();
							response.setStatusCode(HttpStatus.FOUND);
							response.getHeaders().setLocation(URI.create(frontendUrl));
							return response.setComplete();
						}))
				// Let Spring Security handle the OAuth2 Authorization Request flow
				.oauth2Client(Customizer.withDefaults())

				// Logout configuration to perform RP-Initiated OIDC logout with Keycloak
				.logout(logout -> logout
						.logoutUrl("/auth/logout")
						.logoutHandler(new DelegatingServerLogoutHandler(
								new SecurityContextServerLogoutHandler(),
								new WebSessionServerLogoutHandler()))
						.logoutSuccessHandler(this::logoutFromKeycloak))
				.build();
	}

	/**
	 * Performs an RP-Initiated OpenID Connect logout against Keycloak.
	 *
	 * <p>
	 * After the local Spring Security context and web session have been
	 * invalidated, this handler redirects the browser to Keycloak's
	 * {@code end_session_endpoint}, including:
	 * </p>
	 *
	 * <ul>
	 * <li><b>id_token_hint</b> to identify the authenticated OIDC session</li>
	 * <li><b>post_logout_redirect_uri</b> to return the user to the frontend
	 * after the provider session has been terminated</li>
	 * </ul>
	 *
	 * <p>
	 * This ensures that both the local gateway session and the remote Keycloak
	 * Single Sign-On session are fully terminated, preventing automatic
	 * re-authentication on the next login attempt.
	 * </p>
	 *
	 * @param webFilterExchange the current web filter exchange
	 * @param authentication    the current authenticated principal
	 * @return a {@link reactor.core.publisher.Mono} completing the redirect
	 *         response
	 */
	private Mono<Void> logoutFromKeycloak(
			WebFilterExchange webFilterExchange,
			Authentication authentication) {
		var response = webFilterExchange.getExchange().getResponse();
		response.setStatusCode(HttpStatus.FOUND);

		if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
			response.getHeaders().setLocation(URI.create(frontendUrl));
			return response.setComplete();
		}

		var keycloakLogoutUri = UriComponentsBuilder
				.fromUriString(oidcUser.getIssuer().toString())
				.path("/protocol/openid-connect/logout")
				.queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue())
				.queryParam("post_logout_redirect_uri", frontendUrl)
				.build()
				.encode()
				.toUri();

		response.getHeaders().setLocation(keycloakLogoutUri);
		return response.setComplete();
	}
}
