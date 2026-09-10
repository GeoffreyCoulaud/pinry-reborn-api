# The web application arrives: one session with two transports, and a grid that does not jump

Date: 2026-09-10
Status: Draft, awaiting the operator's review; one adversarial review closed, its findings and their
exits recorded in block 1's pull request
Branch of block 1: `feat/webapp-scaffold`
ADRs: `docs/adr/0027-the-web-application-stack.md` (block 1, delivered with this document);
`docs/adr/0026-one-session-two-transports.md` (block 2, which is where the mechanism lands, and
which supersedes part of `docs/adr/0024-three-projects-share-one-repository.md`)

## 1. Goal

Build the API's first client: a web application where a user signs up, signs in, sees their pins in
a grid that does not move under them, and creates a pin from a URL or from a file on disk. Repair,
in the same lot, the four gaps that building it exposes in the API, because a contract change and
its consumer travel together (ADR 0024).

The lot builds no extension, no boards, no tags, no search, no recycle bin, no account screens and
no import or export. Section 7 names them with the observable that would show one had slipped in,
and section 6 files them.

Every design choice below comes from a question put to the operator during Discuss, one at a time.
Section 3 is that record.

## 2. What exists today

The contract serves 31 paths and 47 operations, and no client consumes any of them. Seven
properties of that state carry the whole design. Each was read, none assumed.

```
$ K=api/api-presentation-quarkus/src/main/kotlin/fr/geoffreyCoulaud/pinryReborn/api/presentation/quarkus
$ python3 -c "import json;d=json.load(open('contract/openapi.json'));print(len(d['paths']))"
31
$ grep -n "@Authenticated" $K/controllers/ImageController.kt
51:@Authenticated
$ grep -c "val " $K/dtos/output/PinOutputDto.kt
8
$ grep -n "image" $K/dtos/output/PinOutputDto.kt      # no match, exit status 1
$ grep -n "deleteIfPending" api/api-usecases/src/main/kotlin/fr/geoffreyCoulaud/pinryReborn/api/usecases/DownloadPinImage.kt
105:                    if (imageDownloadRepository.deleteIfPending(pinId) > 0) {
$ grep -n -B1 "fun maxFileBytes\|fun maxPixels" $K/config/ImagesConfig.kt
11-    @WithDefault("31457280") // 30 MiB
12:    fun maxFileBytes(): Long
14-    @WithDefault("50000000") // 50 megapixels
15:    fun maxPixels(): Long
$ grep -n "cors.origins\|info-version" api/api-application/src/main/resources/application.properties
24:api.cors.origins=http://localhost:5173
38:quarkus.smallrye-openapi.info-version=1.0.0
$ grep -n "url = \|fun baseUrl" $K/mappers/PinImageStateMapper.kt $K/config/ApiConfig.kt
$K/mappers/PinImageStateMapper.kt:15:            url = img?.let { "$baseUrl/api/v1/pins/$pinId/image" },
$K/config/ApiConfig.kt:19:    fun baseUrl(): String = "https://${remoteHost()}:${port()}/${basePath()}"
$ grep -n "@PermitAll" $K/controllers/UserController.kt
18:    @PermitAll
```

1. **The image bytes sit behind bearer authentication.** `ImageController` is `@Authenticated` and
   the contract's only security scheme is `http`/`bearer`. A browser sends no `Authorization`
   header for an `<img>`, so no gallery is possible without a second transport. This is the claim
   the whole lot rests on; section 8 says what happens if it is false.
2. **A pin carries no image.** `PinOutputDto` has eight fields and none of them is an image, a URL
   or a dimension. Width and height exist only in `PinImageStateDto`, served one pin at a time by
   `GET /api/v1/pins/{pinId}/image/status`. A grid built on today's contract would issue one request
   per tile.
3. **A download already has a row, and it vanishes on success.** `ImageDownload` carries `pinId`,
   `sourceUrl`, `status` (`PENDING` or `FAILED`), `reasonCode`, `lastError`, `taskId`, `requestedAt`
   and `updatedAt`. It carries no author, and `ImageDownloadModel` declares no relation to
   `PinModel`. `DownloadPinImage` deletes the row when the download succeeds, so the server can
   report what is running and what failed, and cannot report what succeeded.
4. **The deployment's image limits are configured and unpublished.** `ImagesConfig` defaults to 30
   MiB and 50 megapixels, both per deployment. No route serves them, so a client cannot refuse an
   oversized file before uploading it.
5. **Every image URL the API emits is absolute, and built from three configuration keys.**
   `ApiConfig.baseUrl()` interpolates `api.remote_host`, `api.port` and `api.base_path`, whose
   defaults are `0.0.0.0`, `8080` and empty. `PinImageStateMapper` and `ImageMapper` both prefix
   with it, and `ImageController` builds the `Location` header the same way.
6. **CORS names the development server.** `api.cors.origins` defaults to `http://localhost:5173`,
   and `access-control-allow-credentials` appears nowhere in the repository.
7. **The contract declares `1.0.0`** and `contract/frozen/` is empty, so no major is still served
   and the guard refuses nothing but an undeclared bump.

## 3. The questions and the answers

| # | Question | Answer |
|---|---|---|
| A | How does the browser fetch image bytes | By cookie. A signed URL was proposed and withdrawn: a short lived signature is a new cache key on every rotation, so the browser's disk cache stops working. The route already answers `ETag` and `Cache-Control: private, must-revalidate`, so a stable URL buys a 304 rather than a re-download |
| B | Who serves the bundle | Two artefacts, one reverse proxy, one public origin. Not the API, which would recouple the two images against ADR 0024 decision 6. This reverses ADR 0024's cross-origin consequence, and ADR 0026 records the reversal |
| C | How far does the cookie reach | The whole session. The web application never sees the token; the extension keeps the header. One client, one transport |
| D | How does a client declare its transport | One route, a required `transport` field, two responses told apart by their status code |
| E | Where do the grid's dimensions come from | `PinOutputDto` gains a nullable `image` field carrying `status`, `url`, `width` and `height` |
| F | What does the JavaScript gate refuse | A coverage bound on the pure functions, and a named list of user journeys with MSW whose absence fails the gate |
| G | What does the lot deliver | Sign in, the grid, and creating a pin from a URL. Extended by R (sign up) and V (file upload) |
| H | Is the grid virtualised | Yes, and not by hand. Superseded by Q: React Aria's `Virtualizer` replaces TanStack Virtual and the placement function |
| I | What does the user see while the server downloads | A task indicator in the header with a popover, not a zone inside the grid |
| J | Where does the popover's list come from | The server: downloads that are running or failed. Nothing to clean up for a success, whose result is the pin |
| K | What is the shape of `clients/` | The layout ADR 0024 describes, `apps/` and `packages/`, because the extension is close |
| L | Which package manager | pnpm, whose strict tree refuses an undeclared import |
| M | Which packages | `packages/api-client` and `packages/auth`, the second depending on the first |
| N | Which router | TanStack Router, for typed and validated search parameters |
| O | Which languages | English and French from the first lot |
| P | Which i18n library | Paraglide JS: the catalogue is compiled into typed functions, the same mechanism the contract already uses |
| Q | Which UI primitives | React Aria Components, the only maintained set that carries accessible drag and drop, grid selection and virtualisation together |
| R | Is sign up in the lot | Yes. Without it the lot produces nothing usable on a fresh instance |
| S | How does the client learn a download finished | Polling, stopping when the list empties. SSE later, if an import ever fills it |
| T | One theme or two | Light and dark, following the system, with a manual switch |
| U | One lot or two | One, nine blocks, each API block landing before the client block that consumes it |
| V | Can a file be uploaded from disk | Yes. A remote download fails often, and without upload a failed task has no recourse |
| W | Does the lot serve the handshake | Yes, in its own block. Client side validation is impossible without the limits, and a hard coded limit would drift from the deployment |
| X | What happens to CORS | The default origin list empties. The proxy makes the web application same origin, and the extension will add its own |
| Y | What volume must the grid hold | Tens of thousands in the collection, but the catalogue is paginated: what the grid holds is what scrolling accumulates |
| Z | What proves the grid holds it | Nothing in this lot. jsdom has no layout, and a browser in the gate is a permanent cost for a one-off question. Measured in use instead |
| AA | Who computes the tile's height | The tile, through CSS `aspect-ratio`. `WaterfallLayout` has no per-item input, so the layout measures the node and settles once per tile |
| AB | Absolute or relative image URLs | Relative. Three configuration keys leave the critical path, and the one client that needs an absolute URL already configures its base |
| AC | The lockfile against the block budget | `clients/pnpm-lock.yaml` is marked generated and excluded from the count, as `.dagger/.gitattributes` already does for `/sdk/**` |

### Decisions this document settles without an ADR

The review mandate asks where each architectural decision is recorded. Three are settled here and
sent nowhere else, with the reason:

- **Polling rather than SSE** (question S) is a client side scheduling choice over a route the
  contract describes. It adds no protocol: the route is an ordinary `GET`, and moving to SSE later
  adds a route rather than changing one.
- **The image URL's shape** (question AB) is a property of the contract, which is itself the record.
- **The lockfile's exclusion** (question AC) is a repository convention, recorded in
  `clients/.gitattributes` next to the file it exempts and in `clients/AGENTS.md`.

Everything else lands in ADR 0026 (the transport, the cookie's attributes and name, the single
public origin, the CORS default, and the revision of ADR 0024 decisions 9 and its cross-origin
consequence) or ADR 0027 (the stack, and the revision of ADR 0024 decision 11).

## 4. Design

### 4.1 One session token, two transports (block 2)

`BearerAuthenticationMechanism` reads `Authorization`, builds a `TokenAuthenticationRequest` and
hands it to `BearerTokenIdentityProvider`. A second implementation of Quarkus's
`HttpAuthenticationMechanism`, `CookieAuthenticationMechanism`, reads the same token from a cookie
and builds the same request, so the session table, the expiry, the renewal threshold and the
revocation are shared. Nothing about authentication forks: only the extraction of the credential
from the request does. The interface's name says the protocol, not the header; the two
implementations are what name the vehicle.

**Quarkus runs mechanisms in descending order of `getPriority()`**, whose default is 1000.
`BearerAuthenticationMechanism` overrides nothing today, so two mechanisms at 1000 would leave the
order unspecified. The cookie mechanism therefore declares a lower priority, and block 2 carries the
criterion that proves it: a request holding both a valid cookie and an `Authorization` header for a
different session authenticates as the header's.

**The cookie is named `pinry_session` and carries `HttpOnly`, `Secure`, `SameSite=Strict` and
`Path=/`.** `SameSite=Strict` is what closes CSRF, and it suffices because decision B puts the
application and the API on one public origin: every request the application makes is same site, and
no third party site can make the browser attach the cookie. `access-control-allow-credentials` stays
unset, which is the second half: even asked for, the cookie never travels to another origin.
(Corrected: unset is not off. Quarkus 3.37 emits the header for any origin the list matches exactly,
so what closes this half is the empty list below, not the absent key.)

`rememberMe` maps onto the cookie's lifetime. Checked, the cookie is persistent and expires with the
session's thirty days; unchecked, it is a session cookie the browser drops on close, and the
session's own twelve hour expiry still applies server side.

**`SessionCreationInputDto` gains a required `transport` field**, `BEARER` or `COOKIE`. The two
responses are told apart by status code, so no field is ever nullable:

| Transport | Status | Body | `Set-Cookie` |
|---|---|---|---|
| `BEARER` | `201` | `CreatedSessionOutputDto` (token, `expiresAt`, `renewAfter`) | none |
| `COOKIE` | `200` | `ExistingSessionOutputDto` (`expiresAt`, `renewAfter`, `persistent`), which already exists | `pinry_session` |

`POST /api/v1/sessions/current/renew` takes no such field, since it knows the transport of the
request that reached it, and it discriminates its response the same way: `201` with a fresh token
for a bearer session, `200` with `ExistingSessionOutputDto` and a new `Set-Cookie` for a cookie one.
Without that split, a cookie renewal would either hand the web application the token question C
keeps from it, or make `token` nullable, which the table above forbids.
`DELETE /api/v1/sessions/current` and `DELETE /api/v1/sessions` clear the cookie when the request
carried one.

**The contract gains a second security scheme.** `OpenApiApplication` declares the bearer scheme by
hand and SmallRye stamps `quarkus.smallrye-openapi.security-scheme-name` on every `@Authenticated`
operation. Block 2 adds an `apiKey` scheme in `cookie` named `pinry_session`, so that the client
`openapi-typescript` generates in block 3 describes the mechanism the web application actually uses.
(Corrected: declaring it is not enough. SmallRye stamps the first of the two declared schemes and
`quarkus.smallrye-openapi.security-scheme-name` does not steer that choice, so the cookie replaced
the bearer on all 45 protected operations; `SessionSecurityRequirementFilter` puts both on each.)

**`api.cors.origins` becomes empty** (question X). The proxy makes the web application same origin,
so nothing needs an entry until the extension has an identifier. The test resources pin
`api.cors.origins=https://app.test` and win by classpath order, so no existing test observes the
production default: block 2 adds the one that does. (Corrected: measured on Quarkus 3.37, an empty
list refuses every origin rather than allowing all; and SmallRye reads an empty value as null, so
`ApiConfig.Cors.origins()` had to become `Optional<String>` for the boot to survive.)

A required field on an input is a breaking change. The contract goes to `2.0.0` and `oasdiff`
accepts the break because the version declares it; `contract/frozen/` is empty, so no still served
major refuses it.

### 4.2 The pin carries its image, and image URLs become relative (block 4)

`PinOutputDto` gains `image`, nullable, carrying `status`, `url`, `width`, `height`. Null means the
pin has no image at all. The three states a tile tells apart are `READY` (place it with the ratio the
API gives), `PENDING` (the tile stays out of the grid, see 4.8) and `FAILED` (a tile showing
`reasonCode`).

The field is populated for every list that returns pins, so the grid places a page in one request.
The alternative examined and refused was a batch endpoint keyed by pin identifiers: it makes the
first render wait on a second round trip and puts a join back into the client, which is the state
handling this lot exists to avoid.

**Every image URL becomes relative** (question AB): `/api/v1/pins/{pinId}/image`, in
`ImageOutputDto`, in `PinImageStateDto`, in the new `image` field, and in the `Location` header
`ImageController` emits, so that one response never carries two conventions. Property 5 is why: an
absolute URL built from `api.remote_host`, `api.port` and `api.base_path` is correct only in a
deployment that reconfigures all three, and a tile pointing at another origin gets no
`SameSite=Strict` cookie. `ApiConfig.baseUrl()` loses its callers here; whether the member itself
goes is block 4's to decide against its own budget.

Adding a property to a response schema breaks no client, and the URL's shape is a value change
rather than a schema change, so `oasdiff` reads the whole block as additive. The contract goes to
`2.1.0`.

### 4.3 The handshake (block 6)

ADR 0024 decision 6 settles what a handshake carries: the contract's major and minor, and the
deployment's limits, with no capability registry. This block serves it, because 4.8 cannot validate
a file before uploading it otherwise, and a limit hard coded in the bundle would drift from the
deployment that configures it.

`GET /api/v1/handshake`, unauthenticated, carrying the contract version and the two keys of
`ImagesConfig` a client acts on, `maxFileBytes` and `maxPixels`. `ImportsConfig.maxChunkBytes` stays
out, its consumer being the import, which this lot does not build.

**The version is read the way `ContractVersionDeclarationTest` already reads it**, from
`src/main/resources/application.properties`, not from the injected configuration. The test resources
do not declare `quarkus.smallrye-openapi.info-version` and win by classpath order, so a route reading
the injected value would serve a default under test, and a test comparing the response to that same
injected value would be a tautology that passes at any number. The contract goes to `2.2.0`.

### 4.4 The download list (block 7)

`GET /api/v1/me/image-downloads` lists the requester's `ImageDownload` rows, which by property 3 are
exactly those running or failed. `DELETE /api/v1/me/image-downloads/{pinId}` removes one failed row.

**Ownership is a traversal, not a column.** `ImageDownload` has no author and no relation to
`PinModel` (property 3), so the query joins through `pins.author_id`. `PinModel` is a
`SoftDeletableModel`, so the traversal goes through the `Queries` objects that
`agents/engineering.md` mandates, under the Konsist assertions and the
`SoftDeleteStateFilteredOutsideQueries` detekt rule. **A recycled pin's download row is not listed**,
by the same read isolation every other query follows
(`docs/adr/0008-structural-soft-delete-read-isolation.md`): the task centre shows work whose result
the user can still see.

This adds one method, `findByAuthor`, to the six the interface has today.

**Deleting a `PENDING` row is refused with `409`.** The worker owns that row, and
`agents/engineering.md` requires the status to come from `BaseErrorMapper.statusFor`, a `when` over
`ErrorCode` with no `else`. Block 7 therefore adds one `ErrorCode` value, its row in that table, and
the exception the use case throws. The contract goes to `2.3.0`.

### 4.5 The client's tree and its tooling (block 1)

```
clients/
  AGENTS.md   pnpm-workspace.yaml   package.json   .npmrc   .gitattributes
  apps/webapp/       vite.config.ts  tsconfig.json  index.html  src/
  packages/api-client/                    block 3, where its first consumer lands
  packages/auth/                          block 3
```

The packages appear in block 3 rather than block 1, with their content and their consumer at once.
This is decision K's layout, not a retreat from it: an empty package would be a directory, not a
boundary, and block 2 changes the contract the client is generated from, so generating it in block 1
would be work done twice.

`clients/.gitattributes` marks `pnpm-lock.yaml` as `linguist-generated` (question AC), and
`clients/AGENTS.md` states that the block budget excludes it. `.dagger/.gitattributes` is the
precedent, and the reason is the same: the budget measures what a human rereads.

Every version below was read from registry.npmjs.org on 2026-09-10, not remembered. (Corrected: block 1
read the registry again before pinning and three of the rows naming a bare major were behind it, so what
it installed is pnpm 12.3.4, ESLint 10.10.0 and dependency-cruiser 18.2.0. TypeScript, which the table
does not name, is pinned at 6.0.3, the newest major `typescript-eslint` 8.70.0 accepts, its peer range
stopping below 6.1. Every other row matched.)

| Dependency | Version | What it replaces |
|---|---|---|
| Node | 24 LTS in the gate container | Vitest 5 needs `^22.12 \|\| ^24 \|\| >=26`, `@vitejs/plugin-react` 6 needs `^20.19 \|\| >=22.12` |
| pnpm | 10 | |
| Vite | 8.3.0 | The build. `@vitejs/plugin-react` 6.1.1 requires `vite: ^8.0.0` |
| React | 19 | |
| TanStack Query | 5.102.8 | Loading, caching and error state, by hand |
| TanStack Router | 1.170.34 | Typed routes and validated search parameters |
| `openapi-typescript`, `openapi-fetch` | 7.13.0, 0.17.0 | A hand written and hand maintained client |
| React Aria Components | 1.21.1 | Popover, dialog, grid selection, accessible drag and drop, the virtualiser |
| Tailwind CSS | 4.3.3 | |
| Paraglide JS | 2.25.1 | A catalogue compiled into typed functions |
| Vitest, `@vitest/coverage-v8` | 5.0.0 | |
| Testing Library, `user-event`, jsdom | 16.3.3, 14.6.7, 30.0.1 | |
| MSW | 2.15.0 | |
| ESLint, `typescript-eslint` | 9, 8 | |
| dependency-cruiser | 17 | The import boundaries of 4.6 |

Three libraries from the operator's opening list are not here, each for a measured reason.
`react-photo-album` (3.6.1) does not virtualise: its `InfiniteScroll` lowers the fidelity of
offscreen photos and keeps every node mounted. `masonic` (4.1.0) does both but published nothing
since 2025-04-22. TanStack Virtual is redundant once React Aria's `Virtualizer` is in, and it does
not do masonry on its own: pairing it with `react-photo-album` would have meant writing the
placement ourselves.

### 4.6 The gate for `clients/` (block 1)

`dagger call clients-gate`, called by `gate` next to `api-gate`, running `pnpm install --frozen-lockfile`
then typecheck, lint, dependency-cruiser, the Paraglide compile, the catalogue parity test and
Vitest. (Corrected: block 1 runs the Paraglide compile second, right after the install, because what it
emits is what the typecheck reads; the parity test is one of the Vitest cases rather than a step of its
own.) **Continuous integration needs no change**: `validate.yml` already runs
`dagger call --progress=plain gate` and nothing else, which is what ADR 0024 decision 5 buys.

**Coverage covers the pure functions, at 100% of lines and branches**, which is the bound
`api/build.gradle.kts:163` already applies per package on the API side. On a pure function an
uncovered line is a missing test or dead code, never an unreachable defensive branch, so any figure
below 100 would be arbitrary. ADR 0024 decision 11 assumed
the bound would cover everything the architecture separates from the view. There is no domain layer
here to separate, so the bound applies to the modules holding pure functions (formatting, display
derivations, the search parameter codecs) and the view is out of it, as `api-application` is out of
the API's bound today. What remains of decision 11 is its boundary half, below, which has real
objects: the packages. ADR 0027 records the revision.

**The journeys are the real net.** The gate fails when a named journey is missing, not only when a
percentage drops. The list at the end of this lot is: sign up, sign in, sign out, session expiry,
browse the grid and load a second page, open a pin, create a pin from a URL through to the tile
appearing, create a pin by uploading a file, and a failed download surfacing in the task centre.
Each block adds its own and none is removed.

**dependency-cruiser states three rules, and each forbids something that exists.** No cycles. The
view does not import the HTTP client, only `packages/auth` and `packages/api-client` do.
`packages/api-client` does not import `packages/auth`, so the dependency runs one way.

The third rule needs its reason, because the cycle it forbids is one both packages have a claim to:
`auth` needs the typed client for its four session routes, and the client's wrapper needs `auth` to
attach the credential to every request. The direction follows the nature of the two packages rather
than a preference. `api-client` holds a file a command rewrites at every install (ADR 0024
decision 4), and code written by hand should not sit downstream of a generator's output: what
`openapi-typescript` emits can never reference `packages/auth`, and a wrapper that did would make
regeneration a coupling point. So `api-client` exports the types and a `createClient` factory,
knowing nothing about sessions, and `auth` builds on it and exports the authenticated client the
applications consume.

**Catalogue parity is a test, not a compiler flag.** Paraglide has no strict mode: a message missing
from a locale falls back to the base locale, silently, and every other gate step stays green. A
Vitest case compares the key sets of `messages/en.json` and `messages/fr.json` and fails on any
difference. It is the only thing in the gate that can catch a French translation nobody wrote.

### 4.7 The grid (block 5)

`Virtualizer` with `WaterfallLayout`, wrapping a `GridList` in `selectionMode="multiple"` with
`layout="grid"`.

**The tile computes its own height, the layout measures it** (question AA). `WaterfallLayout` takes
no per-item size: read from `@react-stately/layout@4.7.1`, its options are `minItemSize`,
`maxItemSize`, `minSpace`, `maxHorizontalSpace`, `maxColumns`, `dropIndicatorThickness` and
`loaderHeight`, all uniform, and `update()` gives an unmeasured item the column's uniform height
with `estimatedSize = true` until `updateItemSize()` reports the measured one. The tile therefore
carries CSS `aspect-ratio` from the `width` and `height` of 4.2, so it is measured at its true height
on the first layout pass and its column settles once, at mount, before any byte arrives. The field of
4.2 is what makes this work: without it the grid would reflow on every image that finished loading,
which is the failure the lot's title names.

Paging uses `useInfiniteQuery` over the cursor pagination the API already has.
`PaginationOutputDto` carries `previousCursor` as well as `nextCursor`, and `CursorDirectionDto` has
`BACKWARD`, so the number of pages held in memory is capped and pages reload upward on the way back.
What the grid holds is therefore what scrolling accumulates, not the collection (question Y).

The tile requests the `small` or `medium` rendition depending on the column width, through
`GET /api/v1/pins/{pinId}/image?size=`.

A pin opens in a dialog, read only: its image, description, tags and boards. Editing any of them is
another lot.

### 4.8 Creating a pin, and the task centre (block 8)

Two entries, one screen. A URL goes to `POST /api/v1/pins` then `PUT /api/v1/pins/{pinId}/image`
with `PinImageDownloadInputDto`, answered `202`: the pin exists, its image does not yet, and the tile
stays out of the grid. A file goes to the same `PUT` as multipart, answered `200` or `201` with the
image already there, so the tile enters the grid at once. The file is refused client side when it
exceeds the limits of 4.3, before any byte is sent.

The header carries a task indicator with the count from 4.4 and a popover listing each task with its
state. It polls while the list is not empty and stops when it empties. A failed task offers the
`reasonCode`, a retry and an upload from disk, which is the recourse question V exists for.

Optimistic mutation is used where the client can compute the result. Creating a pin from a URL
cannot: the outcome depends on a remote download, so the pin appears when the server says it did.

### 4.9 The contract's version through the lot

| Block | Change | Version | Why |
|---|---|---|---|
| 2 | Required `transport` on an input | `2.0.0` | Breaking, declared |
| 4 | `image` added to a response, URLs made relative | `2.1.0` | Additive |
| 6 | New route | `2.2.0` | Additive |
| 7 | Two new routes | `2.3.0` | Additive |

Blocks 1, 3, 5, 8 and 9 touch no contract and bump nothing. The guard derives the required bump from
the diff against `main` and fails on a version that does not match it, so a block that forgets its
bump is red before review.

## 5. Block table

| # | Branch | Delivers | Green alone |
|---|---|---|---|
| 1 | `feat/webapp-scaffold` | The pnpm workspace, `apps/webapp` with one route, the toolchain of 4.5, `clients-gate` in `.dagger/` called by `gate`, `clients/AGENTS.md`, `clients/.gitattributes`, the root `AGENTS.md` updated (its "Where things live" and gate tables), ADR 0027, this document | `dagger call clients-gate` green on its own and `dagger call gate` calling it, shown by the run's output naming both; removing one journey from the list failing the gate; a deliberate cycle between two modules failing dependency-cruiser; `validate.yml` unchanged in the diff |
| 2 | `feat/session-cookie-transport` | The cookie mechanism, the `transport` field, renewal and revocation following the transport, the cookie security scheme, the empty CORS default, ADR 0026, contract `2.0.0` | A `COOKIE` creation answering `200` with no `token` and a `Set-Cookie` carrying `HttpOnly`, `Secure`, `SameSite=Strict`; the next request authenticating on that cookie with no header; a `BEARER` creation still answering `201` with a token and no `Set-Cookie`; a request holding a cookie and a header for two different sessions resolving to the header's; a cookie renewal answering `200` with no token and a fresh `Set-Cookie`; revocation clearing it; a preflight from an unlisted origin refused with the production default read from `src/main/resources`; the gate refusing the same change with `info-version` left at `1.0.0` |
| 3 | `feat/webapp-auth` | `packages/api-client` generated at install, `packages/auth` with both transports, sign up and sign in screens, French and English catalogues | Sign up, sign in, sign out and session expiry as MSW journeys; a message present in English and absent in French failing the parity test of 4.6; `packages/api-client` importing `packages/auth` failing dependency-cruiser |
| 4 | `feat/pin-image-in-list` | `image` on `PinOutputDto` populated by every list, relative image URLs including `Location`, contract `2.1.0` | A pin with a ready image returning width and height in the list response; a pin with a pending download returning `PENDING` and no dimensions; a pin with no image returning null; every emitted URL starting with `/api/v1/` and no test needing `api.remote_host`; a page of N pins calling the image repository once, counted through a fake in the use case test |
| 5 | `feat/webapp-grid` | The virtualised grid, cursor paging with a page cap, the read only pin dialog | Browsing and loading a second page as a journey; opening a pin as a journey; the tile carrying `aspect-ratio` computed from the response; the rendition size varying with column width |
| 6 | `feat/deployment-handshake` | `GET /api/v1/handshake`, contract `2.2.0` | The route answering unauthenticated; the two limits changing in the response when the configuration keys change; the version in the response equal to the one `src/main/resources/application.properties` declares, read from the file |
| 7 | `feat/image-download-list` | The download list, the deletion of a failed row, contract `2.3.0` | A failed download listed and a successful one absent; a recycled pin's row absent; another user's row absent; deleting a failed row answering `204` and the row gone; deleting a `PENDING` row answering `409` through the new `ErrorCode` |
| 8 | `feat/webapp-pin-creation` | Creation from a URL and from a file, the task centre, polling | Both creation journeys; a failed download surfacing in the task centre as a journey; the polling stopping when the list empties; an oversized file refused before any request leaves |
| 9 | `chore/webapp-lot-wrap` | The holistic review's findings, the backlog reconciled, the handoff | The gate, and each finding named with its exit |

Every block measures its diff with `git diff --numstat` against 600 lines, of which under 200 of
production code (`docs/adr/0018-a-block-is-a-pull-request.md`), as soon as it is first green rather
than at the end. `clients/pnpm-lock.yaml` is excluded by 4.5. Block 1 is still the one at risk: if it
exceeds, it splits into the workspace and the application first, `clients-gate` and ADR 0027 second.
(Corrected: block 2 is the one that exceeded, and it cannot split, because the only seam that leaves
two coherent halves puts a second contract major inside the lot and shifts every version in 4.9.
`contract/openapi.json` joins the exclusions, marked generated in `contract/.gitattributes` for the
same reason as the lockfile; block 2's own figures are in its pull request.)

## 6. Adjacent backlog items

| Item | Exit |
|---|---|
| **Browser-extension CORS origin** (`P1`) | Left open, its reason unchanged: the extension has no stable identifier. Block 2 touches the neighbouring line by emptying `api.cors.origins` (question X), which narrows the item rather than closing it |
| **Import follow-ons** (`P1`) | Not adjacent. Selective import, partial export, merging onto an existing pin and a pin with no medium travelling are import work, and no block here touches the import |
| **Two cold Gradle builds per pull request** (`P2`) | Not adjacent, and this lot makes it slightly worse by adding a second ecosystem to the same gate. The exits ADR 0024 names are unchanged |
| **Flatten the migration history** (Before beta) | Left open, and no block needs it: blocks 4, 6 and 7 add no column and no table, `image` being mapped from rows that already exist and the handshake reading configuration |
| **Populate `contract/frozen/`** (Before beta) | Untouched. The contract breaks in block 2 precisely because the alpha allows it |
| **Audience mechanics** (Features) | Left open, and the lot depends on it staying closed: everything is `@Authenticated` and owner scoped, so the web application has no anonymous view and no shareable link to build |

This lot files one new item: the features the API exposes and the web application does not yet
reach, as a plain list (boards, tags, search, recycle bin, account management, import and export,
editing and deleting a pin).

## 7. Out of scope

Each row names how a reader notices if it changed anyway.

| Not done | Observable |
|---|---|
| The extension. No manifest, no store cycle, no `chrome-extension://` origin | `clients/apps/extension` absent |
| Boards, tags, search and the recycle bin, all of which the API serves today | No route in the router beyond sign up, sign in, the grid and creation |
| Editing or deleting a pin | The pin dialog has no control that issues a `PUT` or a `DELETE` |
| Account management and the import and export screens | No call to `/api/v1/me/exports`, `/api/v1/me/imports` or `/api/v1/me/password` |
| Server side rendering, and any Node process in production | No server in the webapp's image; `pnpm build` emits static files only |
| A capability registry in the handshake | The handshake response has two limit fields and a version, nothing else |
| Selection and drag and drop as features, though the primitives that carry them are chosen | `selectionMode` is set on the grid and no action consumes a selection |
| SSE for download progress | No route serves `text/event-stream` |
| A browser in the gate | No Playwright, no `@vitest/browser-*` in any manifest |
| The reverse proxy's configuration and the webapp's image | No `Caddyfile` and no `clients/apps/webapp/Dockerfile` |

## 8. What is not validated

- **That the image bytes really are unreachable from an `<img>`.** Property 1 is the claim the lot
  rests on, and it was read rather than exercised: `@Authenticated` on the controller and one bearer
  scheme in the contract. If a deployment ever fronted the image route with something that
  authenticates differently, blocks 2 and 3 would be solving a problem that no longer exists, and
  the cookie would remain only as the mechanism the extension does not use. Block 2's first
  criterion exercises it end to end, which is where the reading becomes a measurement.
- **That `WaterfallLayout` settles without a visible jump.** The mechanism is now known (question AA)
  and the remaining unknown is perceptual: how visible the single measurement pass is on the first
  page. jsdom has no layout and question Z settled that no browser enters the gate, so the grid is
  judged in use. If it disappoints, the fallback is a custom `Layout` subclass reading the ratios
  directly, which 4.2 keeps possible.
- **The reverse proxy is not delivered.** Decision B settles the topology; no block writes a Caddy
  configuration, and the deployment documentation belongs to the lot that ships the webapp's image.
  Development uses Vite's proxy, which reproduces the same origin.
- **No measurement of what the JavaScript gate costs.** `pnpm install` on a cold Dagger cache is
  estimated, never measured. Block 1 measures it and this document is corrected in the
  `(Corrected: ...)` form if the number changes anything. (Corrected: measured on 2026-09-10, on a
  workstation, engine v0.21.9. `dagger call clients-gate` takes 15.2 seconds with a warm pnpm store.
  Against an empty one the install alone took 3.6 seconds and the whole function 22.3, measured before
  the bundle build joined it. It changes nothing, and the store is a cache volume, so a workstation pays
  the cold figure once and a runner pays it every time.)
- **That `openapi-typescript` generates a usable client for this contract.** Three known oddities
  meet it: `CursorDto` is passed as a query parameter through a `$ref`, `StreamingOutput` is declared
  under `application/json` for a route that returns image bytes, and SmallRye marks nullable Kotlin
  parameters `"required": true`, so `GET /api/v1/pins/{pinId}/image` declares `animated`, `size` and
  `If-None-Match` as required and nullable at once. That is the route every tile calls. Block 3 is
  the first to find out, and a defect there is an API defect, inside this lot's perimeter.
