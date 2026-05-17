package com.mclavo.gateway.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
class KeycloakRealmRoleExtractor {

    /**
     * Extracts authorities from the <code>realm roles</code> in the given claims.
     *
     * @param claims the claims containing the realm roles
     * @return a collection of granted authorities extracted from the realm roles
     */
    Collection<GrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        Object realmAccessClaim = claims.get("realm_access");

        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }

        Object rolesClaim = realmAccess.get("roles");

        if (!(rolesClaim instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}