package com.mclavo.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class KeycloakRealmRoleExtractorTest {

    private final KeycloakRealmRoleExtractor extractor = new KeycloakRealmRoleExtractor();

    @Test
    void should_mapRealmRolesExactly_when_rolesAlreadyHaveRolePrefix() {
        // given
        var roles = List.of("ROLE_ADMIN", "ROLE_ORDER_MANAGER", "ROLE_CUSTOMER_SUPPORT");

        var claims = Map.<String, Object>of("realm_access", Map.of("roles", roles));

        // when
        var authorities = extractor.extractAuthorities(claims).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // then
        assertThat(authorities)
                .containsExactly("ROLE_ADMIN", "ROLE_ORDER_MANAGER", "ROLE_CUSTOMER_SUPPORT")
                .doesNotContain("ROLE_ROLE_ADMIN");
    }
}
