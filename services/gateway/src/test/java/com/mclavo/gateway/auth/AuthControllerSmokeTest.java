package com.mclavo.gateway.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import jakarta.annotation.Resource;

@SpringBootTest(classes = {
        AuthController.class,
        AuthService.class
})
class AuthControllerSmokeTest {

    @Resource
    private AuthController controller;

    @Resource
    private AuthService authMeResponseMapper;



    @Test
    void should_returnUnauthenticatedFalse_when_noSessionAuthenticationExists() {
        // given
        Authentication authentication = null;

        // when
        AuthMeResponse response = controller.me(authentication).block();

        // then
        assertFalse(response.authenticated());
    }

    @Test
    void should_returnCurrentUserWithoutTokens_when_oidcSessionAuthenticationExists() {
        // given
        var authorities = List.of(
                new SimpleGrantedAuthority("OIDC_USER"),
                new SimpleGrantedAuthority("SCOPE_openid"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));
        var idToken = new OidcIdToken(
                "id-token-value",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of(
                        "sub", "operator-1",
                        "preferred_username", "admin",
                        "email", "admin@ecommerce.local",
                        "name", "Admin User"));
        var principal = new DefaultOidcUser(authorities, idToken, "preferred_username");
        var authentication = new TestingAuthenticationToken(principal, "n/a", authorities);

        // when
        AuthMeResponse response = controller.me(authentication).block();

        // then
        assertTrue(response.authenticated());
        assertEquals(response.subject(), "operator-1");
        assertEquals(response.username(), "admin");
        assertEquals(response.email(), "admin@ecommerce.local");
        assertEquals(response.name(), "Admin User");
        assertEquals(response.authorities(), List.of("OIDC_USER", "SCOPE_openid", "ROLE_ADMIN"));
        assertEquals(response.roles(), List.of("ROLE_ADMIN"));

    }
}
