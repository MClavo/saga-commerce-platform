# Frontend Authentication and Security Design

## API Reference

- [Auth](./api/auth_api.md)

## Purpose

The frontend demonstrates the same security model as the backend: the API Gateway is the browser-facing authentication boundary, and React never owns OAuth2 tokens.

## Target Architecture

```text
React app
  -> checks /auth/me with credentials included
  -> navigates to /auth/login when unauthenticated
API Gateway
  -> starts Spring Security OAuth2 Login
Keycloak
  -> authenticates the Operator
  -> redirects to /login/oauth2/code/keycloak
API Gateway
  -> exchanges the authorization code server-side
  -> stores Authentication and OAuth2AuthorizedClient in the Gateway session
  -> sets an HttpOnly SESSION cookie
React app
  -> calls /auth/me and /api/** with credentials included
API Gateway
  -> uses TokenRelay for protected downstream routes
Microservices
  -> validate JWTs as stateless OAuth2 Resource Servers
```

The browser must not store JWTs in `localStorage` or `sessionStorage`, and JavaScript must not read or manage access tokens or refresh tokens.

## Keycloak Realm

The local realm is `ecommerce`.

The Gateway uses confidential OIDC client `ecommerce-gateway` with Authorization Code flow. Its local redirect URI is:

```text
http://localhost:8222/login/oauth2/code/keycloak
```

Direct Access Grants and Implicit Flow remain disabled. The access token includes `realm_access.roles`, and roles are kept exactly as `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, and `ROLE_CUSTOMER_SUPPORT`.

## Frontend Contract

- Login navigates to `/auth/login` or directly to `/oauth2/authorization/keycloak`.
- Auth state comes from `GET /auth/me`.
- Logout posts to `/auth/logout`.
- Cross-origin requests must use `credentials: "include"`.
- The frontend must not add `keycloak-js`.
- The frontend must not use token storage.
- The frontend must not create custom token cookies.

## Browser-Visible State

Expected browser storage:

- `SESSION`: Spring-managed, opaque, HttpOnly Gateway session cookie.
- No access-token cookie.
- No refresh-token cookie.
- No ID-token cookie.
- No custom JWT cookies.
- No OAuth2 tokens in web storage.

Seeing `SESSION` in DevTools is expected and correct. It identifies the Gateway session; it is not a JWT.

Recommended local session cookie properties:

```text
HttpOnly
SameSite=Lax
Path=/
Secure=false for local HTTP development
```

Use `Secure=true` in production HTTPS deployments.

## CSRF Tradeoff

Gateway CSRF is disabled for this local/demo iteration. The demo relies on an HttpOnly `SESSION` cookie, `SameSite=Lax`, and strict CORS credentials for the configured frontend origin.

This is not a blanket production recommendation. Re-enable CSRF or add equivalent request protections before accepting cross-site unsafe requests in production.

## Route Authorization

- `GET /api/v1/products/**` remains public and must not require a TokenRelay authorized client.
- Product mutations require `ROLE_ADMIN`.
- Orders require `ROLE_ADMIN` or `ROLE_ORDER_MANAGER` at the Gateway.
- Customers require `ROLE_ADMIN` or `ROLE_CUSTOMER_SUPPORT`.
- Payments keep existing role restrictions.

## Downstream Services

Domain services remain stateless OAuth2 Resource Servers. They do not use Gateway sessions or OAuth2 Login; they validate the Bearer JWT relayed by the Gateway.
