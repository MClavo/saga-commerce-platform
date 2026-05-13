## Keycloak Local Development

Keycloak runs from Docker Compose for local development. Realm import lives at `docker/keycloak/ecommerce-realm.json` and mounts into `/opt/keycloak/data/import/ecommerce-realm.json`.

Start Keycloak with:

```bash
docker compose -f docker-compose.infra.yml up -d keycloak
```

The container uses `start-dev --import-realm`. If realm JSON changes and Keycloak already has persisted data, recreate volumes before starting again:

```bash
docker compose -f docker-compose.infra.yml down -v
docker compose -f docker-compose.infra.yml up -d keycloak
```

Local issuer stays `http://localhost:9098/realms/ecommerce` because browser-issued tokens use localhost. Docker services fetch JWKS through Docker DNS with `JWT_JWK_SET_URI=http://keycloak:8080/realms/ecommerce/protocol/openid-connect/certs`.

`ecommerce-frontend` is public OIDC client for local frontend/API testing. `directAccessGrantsEnabled=true` is development convenience for PowerShell/Postman token testing. Future browser frontend should use Authorization Code Flow with PKCE.

Realm users, passwords, and client secrets in `ecommerce-realm.json` are local/dev fixtures only. They are not production secrets.

Redirect URIs must match browser-reachable callback URLs, for example `http://localhost:5173/*`. Do not use Docker service names as browser redirect URIs unless browser can resolve them. Web origins are CORS origins such as `http://localhost:5173`.
