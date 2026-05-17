package com.mclavo.gateway.auth;

import java.util.List;

record AuthMeResponse(
        boolean authenticated,
        String subject,
        String username,
        String email,
        String name,
        List<String> authorities,
        List<String> roles
) {

    static AuthMeResponse anonymous() {
        return new AuthMeResponse(
                false,
                null,
                null,
                null,
                null,
                List.of(),
                List.of()
        );
    }

    static AuthMeResponse authenticated(
            String subject,
            String username,
            String email,
            String name,
            List<String> authorities,
            List<String> roles
    ) {
        return new AuthMeResponse(
                true,
                subject,
                username,
                email,
                name,
                List.copyOf(authorities),
                List.copyOf(roles)
        );
    }
}