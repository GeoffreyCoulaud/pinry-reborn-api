# 0026. One session, two transports, and a single public origin

Status: Accepted
Date: 2026-09-10
Specification: `docs/specs/2026-09-10-web-application.md`, questions A to D and X, and section 4.1
Related: `docs/adr/0024-three-projects-share-one-repository.md`, whose decision 9 this one revises along
with its cross-origin consequence; `docs/adr/0027-the-web-application-stack.md`, which ships the bundle
this cookie serves; `docs/specs/2026-07-21-cors.md`, which this one partially supersedes, its "Bearer
header only and will stay so" being what the cookie transport falsifies (added by block 12, the
both-ways link `agents/writing.md` requires).

## Context

**A browser sends no `Authorization` header for an `<img>`.** `ImageController` is `@Authenticated` and
the contract's only security scheme was `http`/`bearer`, so every image byte the API serves sat behind a
header no `src` attribute can carry. A gallery is the web application's whole point, so either the image
route stops being authenticated, or a second transport arrives. This is the claim the lot rests on, and
block 2's first criterion exercises it end to end rather than reading it off the annotations.

**A signed URL was proposed and withdrawn.** It authenticates without a cookie, but a signature that
rotates is a new cache key on every rotation, so the browser's disk cache stops working. The image route
already answers `ETag` and `Cache-Control: private, must-revalidate`, so a stable URL buys a 304 where a
signed one buys a re-download.

**ADR 0024 decision 9 assumed the two clients would share one mechanism.** It refused a rendering server
with an `httpOnly` cookie because "an extension is necessarily cross origin and token based", and
concluded that a cookie would fork authentication into two mechanisms and empty `packages/auth` of its
purpose. The first half of that reasoning holds: the extension keeps the header. The second half does
not, and this ADR says why: what forks is the extraction of the credential from the request, not the
session, and `packages/auth` is exactly where the fork is hidden from the application above it.

### What was rejected

- **Serving the bundle from the API.** It would put the web application inside the API's image and
  recouple two artefacts ADR 0024 decision 6 versions independently.
- **`access-control-allow-credentials`.** Setting it would let a cookie travel to another origin, which
  is the one thing `SameSite=Strict` exists to prevent. Nothing in the repository sets it.
- **Making `token` nullable so one response shape serves both transports.** A nullable field on a
  response is a field every client has to re-check; a status code is a discriminator every client
  already reads.

## Decision

1. **One session token, two `HttpAuthenticationMechanism` implementations.**
   `BearerAuthenticationMechanism` reads `Authorization`, `CookieAuthenticationMechanism` reads the
   `pinry_session` cookie, and both build the same `TokenAuthenticationRequest` for the one identity
   provider. The session table, the expiry, the renewal threshold and the revocation are therefore
   shared, and nothing about authentication forks but the extraction.

2. **The cookie mechanism runs below the header's.** Quarkus asks mechanisms in descending order of
   `getPriority()`, and every mechanism it ships sits at the default; the cookie declares one below it,
   as `HttpAuthenticationMechanism.DEFAULT_PRIORITY - 1`. A request carrying both a cookie and
   an `Authorization` header for a different session therefore authenticates as the header's, which is
   an observable the block tests rather than an ordering nobody pinned.

3. **The cookie is `pinry_session`, `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/`.**
   `SameSite=Strict` is what closes CSRF, and it suffices because of decision 5: every request the
   application makes is same site, and no third party site can make the browser attach the cookie.
   `rememberMe` maps onto its lifetime: checked, the cookie expires with the session's thirty days;
   unchecked, it carries no expiry and the browser drops it on close, the twelve hour server side expiry
   still applying.

4. **A client declares its transport, and the status code discriminates the answer.**
   `SessionCreationInputDto` gains a required `transport`, `BEARER` or `COOKIE`. Bearer is answered
   `201` with the token; cookie is answered `200` with `ExistingSessionOutputDto` and a `Set-Cookie`.
   Renewal takes no such field, knowing the transport of the request that reached it, and splits the
   same way; both revocations clear the cookie when the request carried one. No field of either body is
   ever nullable, which is the property the split buys.

5. **The web application and the API are one public origin, behind one reverse proxy**, and
   `api.cors.origins` ships empty. Measured on Quarkus 3.37: an empty list refuses every origin rather
   than allowing all, and `access-control-allow-credentials` is emitted only for an origin the list
   matches, so an empty list closes both halves. The browser extension will add its own entry when it
   has a stable identifier. **This revises ADR 0024 decision 9**: the web application still ships as a
   static bundle with no Node runtime, and it no longer authenticates with a token.

6. **The contract declares both schemes, and every protected operation names both.** SmallRye stamps
   exactly one scheme name per operation, the first of the two declared, so `SessionSecurityRequirementFilter`
   replaces that lone requirement with the pair. Without it the published contract would say the
   protected routes accept the cookie alone, which is false for the extension.

## Consequences

- **The contract breaks, and goes to `2.0.0`.** A required field on an input is a break; `oasdiff`
  accepts it because the version declares it, and `contract/frozen/` is empty, so no still served major
  refuses it. The alpha is where this is cheap.
- **`POST /api/v1/sessions/current/renew` answers `201` where it answered `200`.** Every bearer client
  reads a new status for the same body. It is part of the same major.
- **The deployment needs a reverse proxy and this repository does not configure one.** Nothing here
  writes a Caddy file; development reproduces the single origin with Vite's own proxy, and the
  deployment documentation belongs to the lot that ships the web application's image.
- **A deployment that serves the API on plain HTTP cannot use the cookie transport**, `Secure` being
  unconditional. Browsers except `localhost`, which is what keeps development working; anywhere else a
  session token in the clear is the failure this transport would otherwise make silent.
- **The identity carries the transport it arrived on**, as a `sessionTransport` attribute, which is what
  lets a renewal answer with a `Set-Cookie` rather than a token. A third transport would add a value to
  `SessionTransport` and a mechanism, and nothing else.
