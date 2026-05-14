# Auth API

Base path: `/auth`

Authentication is handled by the API Gateway using OAuth2 Login with Keycloak. The frontend does not store JWT tokens. The browser only keeps the gateway session cookie, which must be `HttpOnly`.

Downstream service calls are authenticated by the gateway using token relay.

Gateway access:
- `GET /auth/login` is public.
- `GET /auth/csrf` is public.
- `GET /auth/me` returns the current session state.
- `POST /auth/logout` requires an authenticated session and a valid CSRF token.

## Current User Response

Authenticated response:

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

Unauthenticated response:

```json
{
  "authenticated": false
}
```

## CSRF Response

```json
{
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "token": "generated-csrf-token"
}
```

## Login

```http
GET /auth/login
```

Starts the OAuth2 login flow.

The gateway redirects the browser to the internal Spring Security OAuth2 authorization endpoint, which then redirects to Keycloak.

Response: `302 Found`

Redirect target:

```http
/oauth2/authorization/keycloak
```

Frontend usage:

```ts
window.location.href = "/auth/login";
```

Notes:
- The frontend must not send username or password to the backend.
- The frontend must not store access tokens or refresh tokens.
- Successful login creates a gateway session cookie.

## Get Current User

```http
GET /auth/me
```

Returns the currently authenticated user.

Response when authenticated: `200 OK`

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

Response when unauthenticated: `200 OK`

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

## Get CSRF Token

```http
GET /auth/csrf
```

Returns the CSRF token required for state-changing requests.

Response: `200 OK`

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

## Logout

```http
POST /auth/logout
Content-Type: application/x-www-form-urlencoded
```

Logs out the user from the gateway session.

If Keycloak logout is configured through a logout success handler, the gateway also redirects the browser to the Keycloak logout endpoint so the user can switch accounts cleanly.

Requires a valid CSRF token.

Request:

```http
POST /auth/logout
Content-Type: application/x-www-form-urlencoded

_csrf=generated-csrf-token
```

Response: `302 Found`

Redirect target when only local gateway logout is configured:

```http
/
```

Redirect target when Keycloak logout is enabled:

```http
/keycloak/logout-endpoint
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

Notes:
- Prefer form submission over `fetch` for logout because the browser can naturally follow redirects to Keycloak.
- Local gateway logout alone may not clear the Keycloak SSO session.
- For demo user switching, logout should clear both the gateway session and the Keycloak session.

## Session Model

The frontend must call backend endpoints with credentials included:

```ts
fetch("/api/v1/products", {
  credentials: "include"
});
```

The frontend must not access or persist OAuth2 tokens.

Expected browser storage:
- Gateway session cookie: `HttpOnly`.
- CSRF token cookie or response value: readable by the frontend if required.
- No access token in `localStorage`.
- No refresh token in `localStorage`.
- No JWT managed directly by React.

## Gateway Responsibilities

The gateway is responsible for:
- Starting the OAuth2 login flow.
- Maintaining the authenticated user session.
- Exposing the current user through `/auth/me`.
- Providing CSRF tokens through `/auth/csrf`.
- Handling logout through `/auth/logout`.
- Relaying the access token to downstream services.
- Enforcing route-level authorization before forwarding requests.
