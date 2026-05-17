# Auth API

Base path: `/auth`

Authentication is handled by `gateway-service` as a Backend-for-Frontend using Spring Security OAuth2 Login with Keycloak. The browser receives only a Spring-managed HttpOnly `SESSION` cookie; access tokens, refresh tokens, and ID tokens stay server-side in the Gateway session/authorized-client state.

Downstream services do not use browser sessions. They receive `Authorization: Bearer <access_token>` from Gateway TokenRelay and validate JWTs as stateless OAuth2 Resource Servers.

## Endpoints

- `GET /auth/login` is public and redirects to `/oauth2/authorization/keycloak`.
- `GET /auth/me` is public and returns `authenticated=false` when no Gateway session exists.
- `POST /auth/logout` invalidates the Gateway session.

There is no `/auth/csrf` endpoint in this iteration because Gateway CSRF is disabled for the local/demo API scope.

## Login

```http
GET /auth/login
```

Starts Spring Security OAuth2 Login by redirecting the browser to:

```http
/oauth2/authorization/keycloak
```

Spring Security handles the standard callback:

```http
/login/oauth2/code/keycloak
```

The frontend must not call Keycloak token endpoints and must not store OAuth2 tokens.

## Get Current User

```http
GET /auth/me
```

Authenticated response:

```json
{
  "authenticated": true,
  "subject": "keycloak-user-id",
  "username": "admin",
  "email": "admin@ecommerce.local",
  "name": "Admin User",
  "authorities": [
    "OIDC_USER",
    "SCOPE_openid",
    "SCOPE_profile",
    "SCOPE_email",
    "ROLE_ADMIN",
    "ROLE_ORDER_MANAGER",
    "ROLE_CUSTOMER_SUPPORT"
  ],
  "roles": [
    "ROLE_ADMIN",
    "ROLE_ORDER_MANAGER",
    "ROLE_CUSTOMER_SUPPORT"
  ]
}
```

Unauthenticated response:

```json
{
  "authenticated": false
}
```

`/auth/me` never returns raw tokens.

Frontend usage:

```ts
const user = await fetch("/auth/me", {
  credentials: "include"
}).then(response => response.json());
```

## Logout

```http
POST /auth/logout
```

Invalidates the Gateway `SESSION` and redirects to the configured frontend URL. This iteration keeps logout local to the Gateway; Keycloak/OIDC logout can be added later if account switching requires ending the Keycloak SSO session too.

Frontend usage:

```ts
const form = document.createElement("form");
form.method = "POST";
form.action = "/auth/logout";
document.body.appendChild(form);
form.submit();
```

## Session Model

Expected browser-visible state:

- `SESSION`: an opaque, HttpOnly Spring session identifier.
- No `access_token` cookie.
- No `refresh_token` cookie.
- No `id_token` cookie.
- No JWT in `localStorage` or `sessionStorage`.
- No frontend-managed `Authorization: Bearer` header.

`SESSION` is correct because the Gateway is the BFF and owns browser authentication state. It is not a JWT and does not expose token contents to JavaScript.

## CSRF Tradeoff

Gateway CSRF is disabled for this local/demo iteration. The current mitigation is an HttpOnly `SESSION` cookie, `SameSite=Lax`, and strict Gateway CORS credentials for the frontend origin. Re-enable CSRF or add equivalent request protections before treating cross-site unsafe requests as production-ready.
