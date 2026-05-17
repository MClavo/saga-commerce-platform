package com.mclavo.gateway.security;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class KeycloakOidcUserMapper {

    private final KeycloakRealmRoleExtractor roleExtractor;

    KeycloakOidcUserMapper(KeycloakRealmRoleExtractor roleExtractor) {
        this.roleExtractor = roleExtractor;
    }

    /**
     * Maps the OIDC user from the user request and the original OIDC user.
     *
     * @param userRequest the OIDC user request
     * @param oidcUser    the original OIDC user
     * @return the mapped OIDC user with extracted authorities
     */
    OidcUser map(OidcUserRequest userRequest, OidcUser oidcUser) {
        var authorities = authoritiesFrom(oidcUser);

        var usernameAttribute = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return oidcUserFrom(oidcUser, authorities, usernameAttribute);
    }

    /**
     * Extracts authorities from the OIDC user and the realm roles.
     *
     * @param oidcUser the OIDC user
     * @return a set of granted authorities
     */
    private Set<GrantedAuthority> authoritiesFrom(OidcUser oidcUser) {
        var authorities = new LinkedHashSet<GrantedAuthority>();

        authorities.addAll(oidcUser.getAuthorities());
        authorities.addAll(roleExtractor.extractAuthorities(oidcUser.getClaims()));

        return authorities;
    }

    /**
     * Creates a new OIDC user with the given authorities and username attribute.
     *
     * @param oidcUser          the original OIDC user
     * @param authorities       the set of granted authorities
     * @param usernameAttribute the username attribute name
     * @return a new OIDC user with the specified authorities and username attribute
     */
    private OidcUser oidcUserFrom(
            OidcUser oidcUser,
            Set<GrantedAuthority> authorities,
            String usernameAttribute) {

        if (!StringUtils.hasText(usernameAttribute)) {
            return new DefaultOidcUser(
                    authorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo());
        }

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                usernameAttribute);
    }
}