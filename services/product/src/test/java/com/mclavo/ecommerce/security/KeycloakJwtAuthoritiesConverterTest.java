package com.mclavo.ecommerce.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtAuthoritiesConverterTest {

    private final KeycloakJwtAuthoritiesConverter converter = new KeycloakJwtAuthoritiesConverter();

    @Test
    void should_preserveScopesAndMapRealmRolesExactly_when_jwtContainsScopeAndRealmAccessRoles() {
        // given
        var jwt = Jwt.withTokenValue("access-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("scope", "openid email profile")
                .claim("realm_access", Map.of(
                        "roles", List.of("ROLE_ADMIN", "ROLE_ORDER_MANAGER", "ROLE_CUSTOMER_SUPPORT")))
                .build();

        // when
        var authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities)
                .contains("SCOPE_openid", "SCOPE_email", "SCOPE_profile")
                .contains("ROLE_ADMIN", "ROLE_ORDER_MANAGER", "ROLE_CUSTOMER_SUPPORT")
                .doesNotContain("ROLE_ROLE_ADMIN");
    }
}
