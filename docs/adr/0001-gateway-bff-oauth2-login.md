# Gateway BFF OAuth2 Login

We use the API Gateway as a Backend-for-Frontend for browser authentication: Spring Security OAuth2 Login creates a server-side Gateway session backed by a Spring `SESSION` cookie, and Spring OAuth2 Client support keeps the authorized client and tokens server-side. Downstream services remain stateless OAuth2 Resource Servers, and the Gateway relays the user access token to them with TokenRelay.

This prevents JavaScript from storing or reading access tokens, refresh tokens, ID tokens, or JWT cookies. The tradeoff is that the Gateway is stateful; this iteration uses in-memory WebSession storage for the local demo, so horizontal Gateway scaling would require sticky sessions or shared session storage later.

For the current local/demo scope, Gateway CSRF protection is disabled to keep API calls simple while relying on an HttpOnly `SESSION` cookie, `SameSite=Lax`, and strict CORS credentials. CSRF should be revisited before treating cross-site unsafe requests as production-ready.
