package com.mclavo.gateway.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
class AuthService {

    /**
     * Authenticates the user based on the given authentication object and returns
     * an AuthMeResponse containing the user's information and authorities.
     *
     * @param authentication the authentication object containing the user's details
     * @return an AuthMeResponse with the user's information and authorities
     */
    AuthMeResponse authenticate(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return AuthMeResponse.anonymous();
        }

        if (!(authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal)) {
            return AuthMeResponse.anonymous();
        }

        var authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var roles = authorities.stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();

        return AuthMeResponse.authenticated(
                subject(principal),
                username(principal),
                email(principal),
                name(principal),
                authorities,
                roles
        );
    }

    private static String subject(OAuth2AuthenticatedPrincipal principal) {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getSubject();
        }

        return principal.getAttribute("sub");
    }

    private static String username(OAuth2AuthenticatedPrincipal principal) {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getPreferredUsername();
        }

        return principal.getAttribute("preferred_username");
    }

    private static String email(OAuth2AuthenticatedPrincipal principal) {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }

        return principal.getAttribute("email");
    }

    private static String name(OAuth2AuthenticatedPrincipal principal) {
        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getFullName();
        }

        return principal.getAttribute("name");
    }
}