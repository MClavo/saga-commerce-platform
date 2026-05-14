# Frontend Authentication and Security Design

## Purpose

The frontend must demonstrate the same security model as the backend. Authentication is not a decorative login page; it is part of the system architecture.

The final design uses the API Gateway as the browser-facing authentication boundary. React does not authenticate directly with Keycloak and does not store OAuth2 tokens.

## Target Architecture

Use the API Gateway as a Backend-for-Frontend authentication layer.

```text
React app
  -> calls /auth/login
API Gateway
  -> starts OAuth2 Login
Keycloak
  -> authenticates the user
  -> redirects back to the API Gateway
API Gateway
  -> exchanges the authorization code server-side
  -> creates a server-managed session
  -> sets an HttpOnly session cookie
React app
  -> calls /auth/me and /api/** with credentials: include
API Gateway
  -> relays the access token to downstream services
Microservices
  -> validate JWTs as OAuth2 Resource Servers
```

The browser must not store JWTs in `localStorage` or `sessionStorage`.

The React app must not read or manage access tokens or refresh tokens.

## Keycloak Realm

Realm:

```text
ecommerce
```

Recommended gateway client:

```text
ecommerce-gateway
```

Recommended client properties:

- OpenID Connect client.
- Confidential client.
- Authorization Code Flow enabled.
- Direct Access Grants disabled.
- Service accounts disabled unless explicitly needed for another technical flow.
- Redirect URI points to the API Gateway OAuth2 callback.

Local redirect URI:

```text
http://localhost:8222/login/oauth2/code/keycloak
```

Recommended local post-logout redirect URI:

```text
http://localhost:8222
```

The existing `ecommerce-frontend` public client may remain only as a fallback or comparison path for a pure SPA + PKCE flow. It should not be the primary production-style design.

Demo users:

| User | Password | Roles |
|---|---|---|
| `admin` | `admin` | `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, `ROLE_CUSTOMER_SUPPORT` |
| `orders` | `orders` | `ROLE_ORDER_MANAGER` |
| `support` | `support` | `ROLE_CUSTOMER_SUPPORT` |

## Gateway Responsibilities

The API Gateway is responsible for:

- Starting the OAuth2 login flow.
- Maintaining the authenticated browser session.
- Exposing the current user through `/auth/me`.
- Providing CSRF tokens through `/auth/csrf`.
- Handling logout through `/auth/logout`.
- Ending the Keycloak SSO session when logout is configured with Keycloak logout.
- Relaying the access token to downstream services.
- Enforcing route-level authorization before forwarding requests.

The gateway should be both:

- an OAuth2 Client for browser login;
- an OAuth2 Resource Server only if Bearer-token compatibility is still required for non-browser clients or tests.

## Session and Token Model

Expected browser storage:

- Gateway session cookie: `HttpOnly`.
- CSRF token cookie or CSRF response value: readable by the frontend when needed.
- No access token in `localStorage`.
- No refresh token in `localStorage`.
- No JWT managed directly by React.

Recommended session cookie properties:

```text
HttpOnly
SameSite=Lax
Path=/
Secure=true in production
Secure=false only for local HTTP development
```

If using the `__Host-` cookie prefix in production:

- `Secure` must be enabled.
- `Domain` must not be set.
- `Path` must be `/`.

## Auth API

Base path:

```text
/auth
```

These endpoints are owned by the API Gateway.

### Login

```http
GET /auth/login
```

Starts the OAuth2 login flow.

The gateway redirects the browser to the internal Spring Security OAuth2 authorization endpoint:

```http
/oauth2/authorization/keycloak
```

The frontend should not call this internal endpoint directly.

Frontend usage:

```ts
window.location.href = "/auth/login";
```

Notes:

- The frontend must not send username or password to the backend.
- The frontend must not store access tokens or refresh tokens.
- Successful login creates a gateway session cookie.
- The authentication UI should be Keycloak-hosted.

### Get Current User

```http
GET /auth/me
```

Returns the currently authenticated user.

Response when authenticated:

```json
{
  "authenticated": true,
  "username": "admin",
  "email": "admin@ecommerce.local",
  "name": "Admin User",
  "authorities": [
    "ROLE_ADMIN",
    "ROLE_ORDER_MANAGER",
    "ROLE_CUSTOMER_SUPPORT"
  ]
}
```

Response when unauthenticated:

```json
{
  "authenticated": false
}
```

Frontend usage:

```ts
const user = await fetch("/auth/me", {
  credentials: "include"
}).then(response => response.json());
```

The frontend should use `/auth/me` as the source of truth for authentication state and role-aware rendering.

### Get CSRF Token

```http
GET /auth/csrf
```

Returns the CSRF token required for state-changing requests when using cookie-based sessions.

Response:

```json
{
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "generated-csrf-token"
}
```

Frontend usage for JSON requests:

```ts
const csrf = await fetch("/auth/csrf", {
  credentials: "include"
}).then(response => response.json());

await fetch("/api/v1/orders", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    [csrf.headerName]: csrf.token
  },
  body: JSON.stringify(payload)
});
```

### Logout

```http
POST /auth/logout
Content-Type: application/x-www-form-urlencoded
```

Logs out the user from the gateway session.

If Keycloak logout is configured through a logout success handler, the gateway also redirects the browser to the Keycloak logout endpoint. This is recommended for the demo because reviewers need to switch between `admin`, `orders`, and `support` cleanly.

Request:

```http
POST /auth/logout
Content-Type: application/x-www-form-urlencoded

_csrf=generated-csrf-token
```

Response:

```http
302 Found
```

Frontend usage:

```ts
async function logout() {
  const csrf = await fetch("/auth/csrf", {
    credentials: "include"
  }).then(response => response.json());

  const form = document.createElement("form");
  form.method = "POST";
  form.action = "/auth/logout";

  const input = document.createElement("input");
  input.type = "hidden";
  input.name = csrf.parameterName;
  input.value = csrf.token;

  form.appendChild(input);
  document.body.appendChild(form);
  form.submit();
}
```

Prefer form submission over `fetch` for logout because the browser can naturally follow redirects to Keycloak and then back to the application.

## Removed or Rejected Auth Endpoints

The frontend should not depend on these endpoints:

```http
GET /auth/callback
POST /auth/switch-user
```

`/auth/callback` is not a public frontend contract. Spring Security owns the OAuth2 callback internally at:

```http
/login/oauth2/code/keycloak
```

`/auth/switch-user` is unnecessary. User switching should be implemented as:

```text
POST /auth/logout
  -> clear gateway session
  -> clear Keycloak SSO session
  -> login again through /auth/login
```

Do not implement user switching by storing multiple JWTs in the frontend.

## Frontend Request Model

All frontend calls to the gateway must include credentials:

```ts
fetch("/api/v1/products", {
  credentials: "include"
});
```

For state-changing requests, include CSRF:

```ts
fetch("/api/v1/products", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    [csrf.headerName]: csrf.token
  },
  body: JSON.stringify(payload)
});
```

The frontend must not add `Authorization: Bearer <token>` manually in this model.

Token forwarding belongs to the API Gateway through TokenRelay.

## Frontend Login UX

Recommended screens/components:

- `LoginPage`
- `ProtectedRoute`
- `RoleGuard`
- `UserMenu`
- `DemoAccountsDialog`
- `UnauthorizedPage`
- `SessionExpiredDialog`

Do not create an `AuthCallbackPage` for the primary BFF flow. The OAuth2 callback is handled by the gateway, not by React.

Login page content:

- short explanation: “Sign in with Keycloak”.
- `Login with Keycloak` button.
- demo accounts table showing username, password, and roles.

Do not build a custom username/password form in React for the main flow. Use Keycloak-hosted login.

## User Switching UX

Add a user menu in the top-right corner.

Show:

- current username;
- email if available;
- active roles;
- `Logout` action;
- optional `Demo accounts` action.

Expected behavior:

1. User clicks `Logout` or `Switch user`.
2. Frontend submits `POST /auth/logout` with CSRF.
3. Gateway clears the local session.
4. Gateway redirects to Keycloak logout when configured.
5. Browser returns to the application.
6. User starts login again through `/auth/login`.
7. User logs in with another demo account.
8. Frontend refreshes `/auth/me`.
9. Role-based UI updates.

A separate `/auth/switch-user` endpoint is not required.

## Role-Based UI Rules

The frontend should hide or disable actions based on authorities returned by `/auth/me`.

Backend authorization is still the source of truth. UI role checks are for usability, not security enforcement.

### `ROLE_ADMIN`

Can access:

- product creation;
- product updates;
- stock adjustments;
- payment demo confirmation;
- payment demo failure;
- full demo flow actions.

### `ROLE_ORDER_MANAGER`

Can access:

- order list;
- order detail;
- saga flow inspection;
- payment inspection.

### `ROLE_CUSTOMER_SUPPORT`

Can access:

- customer list;
- customer detail;
- customer support workflows.

## Route Protection

Recommended behavior:

- Public routes: login and public product catalog.
- Authenticated routes: saga demo order creation.
- Order manager/admin routes: orders, payments, saga inspection.
- Customer support/admin routes: customers.
- Admin routes: product mutations and payment demo actions.

Example route-level intent:

```text
/dashboard                  authenticated
/products                   public
/products/new               ROLE_ADMIN
/products/:id/edit          ROLE_ADMIN
/customers                  ROLE_CUSTOMER_SUPPORT or ROLE_ADMIN
/orders                     ROLE_ORDER_MANAGER or ROLE_ADMIN
/orders/:id/flow            ROLE_ORDER_MANAGER or ROLE_ADMIN
/payments                   ROLE_ORDER_MANAGER or ROLE_ADMIN
/saga-demo                  authenticated
```

## Gateway Authorization Alignment

Frontend route guards should mirror gateway authorization rules.

Expected gateway access model:

```text
GET    /api/v1/products/**                    public
POST   /api/v1/products/**                    ROLE_ADMIN
PUT    /api/v1/products/**                    ROLE_ADMIN
PATCH  /api/v1/products/**                    ROLE_ADMIN
DELETE /api/v1/products/**                    ROLE_ADMIN

POST   /api/v1/orders/**                      authenticated
GET    /api/v1/orders/**                      ROLE_ORDER_MANAGER or ROLE_ADMIN

/api/v1/customers/**                          ROLE_CUSTOMER_SUPPORT or ROLE_ADMIN

GET    /api/v1/payments/**                    ROLE_ORDER_MANAGER or ROLE_ADMIN
POST   /api/v1/payments/demo/orders/*/confirm ROLE_ADMIN
POST   /api/v1/payments/demo/orders/*/fail    ROLE_ADMIN
Other  /api/v1/payments/**                    denied
```

## Local Development

Preferred local shape:

```text
http://localhost:5173        React/Vite app
http://localhost:8222        API Gateway
```

During Vite development, proxy auth and API requests to the gateway:

```ts
export default defineConfig({
  server: {
    proxy: {
      "/auth": "http://localhost:8222",
      "/api": "http://localhost:8222",
      "/oauth2": "http://localhost:8222",
      "/login": "http://localhost:8222"
    }
  }
});
```

The frontend should call relative paths:

```ts
fetch("/auth/me", { credentials: "include" });
fetch("/api/v1/products", { credentials: "include" });
```

This reduces CORS and cookie issues during local development.

If the frontend and gateway are on different origins:

- allow the exact frontend origin;
- allow credentials;
- do not use wildcard CORS with credentials;
- align `SameSite` and `Secure` settings with local HTTP/HTTPS.

## Security Rules

Avoid:

- `localStorage` tokens;
- `sessionStorage` tokens;
- direct access grants from a React username/password form;
- long-lived refresh tokens exposed to browser JavaScript;
- custom login forms that collect Keycloak credentials in React;
- frontend-managed Bearer tokens in the BFF flow;
- disabling CSRF when using cookie-based sessions.

Acceptable only as a temporary fallback:

- SPA-only Authorization Code + PKCE through the Keycloak JavaScript adapter;
- tokens kept in memory only.

This fallback is not the preferred final design for this project.

## Portfolio Value

This security design demonstrates:

- Keycloak integration;
- API Gateway as OAuth2 Client/BFF;
- Authorization Code Flow handled server-side;
- HttpOnly browser session handling;
- CSRF protection for cookie-based sessions;
- TokenRelay from gateway to downstream services;
- microservices as OAuth2 Resource Servers;
- role-aware UI;
- safe user switching through real logout;
- clear separation between browser session, Keycloak tokens, and backend authorization.

This is more valuable than a custom decorative login form or a SPA that manually stores JWTs.
