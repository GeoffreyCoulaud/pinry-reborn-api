# Handoff: the web application arrives, and the API grows the four things it needed

Date: 2026-09-11
Specification: `docs/specs/2026-09-10-web-application.md`
Decisions: `docs/adr/0026-one-session-two-transports.md`,
`docs/adr/0027-the-web-application-stack.md`
Tier: Spec, ten code blocks and Wrap's closing block, one teammate each
(`docs/adr/0023-act-in-a-teammate-per-block.md`).
Base commit: `356fe306`. Pull requests: 98 (block 1), 99 (2), 100 (3), 101 (4), 102 (5), 103 (6),
104 (7), 105 (8), 106 (9), and block 10's, which carries this document.

## Current state

Nine code blocks are merged and the tenth is this document's own pull request. What is open is the
holistic review over the whole lot, and after it Wrap's closing block, which is the lot's eleventh:
the review's findings, the backlog reconciled, and this document corrected.

**A user can now sign up, sign in, see their pins in a grid that does not jump, create a pin from an
address or from a file on disk, and watch a download run or fail.** Nothing else: no boards, no
tags, no search, no recycle bin, no account screens, no import or export, and no extension. Section
7 of the specification names each and the observable that would show one had slipped in.

Nothing here is deployed. The bundle is built by `pnpm build` and by nothing else: no reverse proxy
configuration and no webapp image exists, and the deployment that makes the application same origin
with the API is the next lot's (see *Next step*).

The contract went from `1.0.0` to `3.3.0` across five blocks, two of them breaking. `contract/frozen/`
is still empty, which is what let the two breaks land: the alpha's policy is the README's.

## What was built

**Block 1, the workspace and its gate** (PR 98). `clients/` as a pnpm workspace, `apps/webapp` with
one route, the toolchain of specification 4.5 pinned from the registry, `clients/AGENTS.md`,
`clients/.gitattributes`, ADR 0027 and the specification itself. `dagger call clients-gate` joins
`gate` next to `api-gate`, and `validate.yml` is untouched, which is what ADR 0024 decision 5 buys.
The gate's teeth are the journey list, the catalogue parity test, dependency-cruiser's three rules
and a coverage bound of 100% over `src/lib/**`; each was proved by the mutation that makes it fail.
Measured: `clients-gate` takes 15.2s with a warm pnpm store and 22.3s against an empty one.

**Block 2, one session and two transports** (PR 99, ADR 0026, contract `2.0.0`).
`CookieAuthenticationMechanism` reads the same token from `pinry_session` and builds the same
`TokenAuthenticationRequest` as the bearer mechanism, at a lower priority so a request carrying both
resolves to the header's. `SessionCreationInputDto` gains a required `transport`, and the two
answers are told apart by status code, `201` with a token or `200` with a `Set-Cookie`; renewal and
revocation follow the transport of the request that reached them. `api.cors.origins` empties.

**Block 3, the two packages and the credentials screens** (PR 100). `packages/api-client` is
`openapi-typescript` output produced by a `prepare` script and committed nowhere;
`packages/auth` owns the transport and exports the authenticated client. Sign up, sign in, sign out
and session expiry are journeys, and the catalogues carry English and French.

**Block 4, a pin carries its image** (PR 101, contract `2.1.0`). `PinOutputDto` gains `image`,
populated by every payload that carries a pin, so the grid places a page from one response. Every
URL the API emits becomes relative, the two `Location` headers of pin and board creation included,
and `ApiConfig.baseUrl()` goes with `remoteHost()`, `basePath()` and the `api.remote_host` key.

**Block 5, the contract declares what it emits** (PR 102, contract `3.0.0`). `CursorDto` carries
`@Schema(type = STRING)`, so the opaque cursor is a string in all eight positions rather than an
object a client must never read; `PinImageStateDto.status` and `ReplacementDto.status` become
presentation enums mapped by an exhaustive `when`. `reasonCode` stays a string, deliberately: a
response enum gaining a value is a break, and `DownloadReason` grows.

**Block 6, the grid** (PR 103). React Aria's `Virtualizer` with `WaterfallLayout` over a `GridList`,
cursor paging through `useInfiniteQuery`, and a read only pin dialog. The tile carries CSS
`aspect-ratio` from the dimensions of block 4, so its column settles at mount before a byte of the
image arrives. The page cap the specification asked for was measured, found to drop pages nothing
reloads, and dropped: what caps the grid's memory is the virtualiser.

**Block 7, the handshake** (PR 104, contract `3.1.0`). `GET /api/v1/handshake`, unauthenticated,
carrying `contractVersion`, `limits.maxFileBytes`, `limits.maxPixels` and the four `renditionSizes`.
The sizes travel because block 6's tile picked its rendition on a breakpoint no route published.

**Block 8, the download list** (PR 105, contract `3.2.0`). `GET /api/v1/me/image-downloads` answers
the requester's running and failed rows, newest first, a recycled pin's row left out;
`DELETE /api/v1/me/image-downloads/{pinId}` drops one settled row and refuses a running one with
`409 IMAGE_DOWNLOAD_IN_PROGRESS`.

**Block 9, the download holds its pin** (PR 106, no contract change). `ImageDownloadModel` gains a
`@ManyToOne` on the `pin_id` column it already had, so `findByAuthor` is one statement through the
query beans instead of a raw subquery and a second query. `1.22.sql` is a hand written table rebuild
that adds the foreign key `1.5` never wrote, SQLite being unable to add one in place.

**Block 10, creating a pin and the task centre** (this pull request, contract `3.3.0`). `/pins/new`
carries both entries on one screen: an address goes to `POST /api/v1/pins` then `PUT
/api/v1/pins/{pinId}/image` as JSON, answered `202`, and the tile stays out of the grid until the
download settles; a file goes to the same `PUT` as multipart and its tile enters at once. The header
carries the task centre, which polls while the server still has work and rereads the grid when a
download settles, and a failed row offers the address again, a file from disk, or forgetting it.
`PinCreationInputDto.sourceMediaUrl` becomes optional, without which the file entry cannot create a
pin at all.

## Pitfalls, in the order they cost time

- **MSW must intercept before the application builds its client.** `server.listen()` runs at the top
  of `src/test/setup.ts` and not in `beforeAll`: `openapi-fetch` reads `globalThis.fetch` when the
  client is built, `api.ts` builds one while it is imported, and a hook runs after that import.
  Started late, every journey reaches the real network.
- **jsdom lays nothing out, and react-aria reports an infinite viewport under test.** Every tile
  renders, `maxColumns`, `maxItemSize` and `maxHorizontalSpace` must all be finite or the layout
  computes `NaN`, and a tile's column measures zero. jsdom also implements no `IntersectionObserver`,
  which the load more sentinel is: `src/test/setup.ts` stubs it as reached.
- **jsdom drops an uploaded file's bytes through `new FormData(form)`.** `input.files[0]` carries the
  two bytes the journey wrote; the same file read back out of a `FormData` built from the form
  carries none. The creation screen therefore holds the chosen file in state and reads the form for
  its text fields alone. A screen that read the file from the form would pass every unit test and
  upload nothing.
- **A multipart body does not read back the same way on every Node.** The upload journey parsed the
  request body on Node 22 and threw on the gate's Node 24, where the failure surfaced as "That pin
  could not be added." rather than as a parse error. The journey now asserts the request's media
  type, which is what tells the two entries apart on one route, and reads no body.
- **React Aria's overlays ship their own hidden "Dismiss" buttons.** A task centre control labelled
  `Dismiss` made `getByRole("button", { name: "Dismiss" })` ambiguous inside the popover. The label
  is "Forget it" now; any UI text that collides with a library's accessible names will do the same.
- **SmallRye stamps the first declared security scheme on every protected operation**, and
  `quarkus.smallrye-openapi.security-scheme-name` does not steer that choice, so declaring the cookie
  scheme replaced the bearer on all 45 of them. `SessionSecurityRequirementFilter` puts both on each.
- **An empty `api.cors.origins` refuses every origin rather than allowing all**, measured on Quarkus
  3.37, and SmallRye reads an empty value as null, so `ApiConfig.Cors.origins()` had to become
  `Optional<String>` for the boot to survive.
- **A comment over four lines fails `CommentCarriesDocumentation`**, which counts `/**` and `*/`.
  It cost a red gate in three separate blocks.
- **Ebean refuses `@Id` on an association.** The identity property stays `pinId`, and the relation is
  a second property on the same column with `insertable = false, updatable = false`.
- **A `git push` run in the background exits 0 without pushing**, the `pre-push` hook's gate output
  looking like a successful push. Push in the foreground and check `git ls-remote`.

## What is not validated

- **That the image bytes really are unreachable from an `<img>` without the cookie.** Block 2's first
  criterion exercises the cookie end to end, which is the measurement specification section 8 asked
  for; what stays unread is a deployment that fronts the image route with something else.
- **That `WaterfallLayout` settles without a visible jump.** The mechanism is known and the tile
  carries its ratio before any byte arrives, but the remaining question is perceptual and jsdom has
  no layout. Question Z settled that no browser enters the gate, so the grid is judged in use.
- **That the grid holds tens of thousands of pins.** Same reason, and the page cap that would have
  bounded the query is gone (backlog).
- **The reverse proxy is not delivered**, so the single public origin decision B rests on exists in
  development only, through Vite's proxy. Nothing has run the application against a real API.
- **The pixel half of the file check.** `uploadRefusal` is unit tested at 100%, but jsdom decodes no
  image: `createImageBitmap` is stubbed at a fixed size, so what the journey exercises is the weight
  half. A browser is what would exercise the other.
- **No client has ever spoken to a running API.** Every journey answers through MSW, and the contract
  is what says the two agree. A mismatch between the document and the server would pass the gate.

## The lot against ADR 0018's budget

| Block | Total | Production |
|---|---|---|
| 1 | 503 | 97 (181 counting the pipeline and the hook) |
| 2 | 791 | **206** |
| 3 | 594 | **262** |
| 4 | 400 | 111 |
| 5 | 221 | 51 |
| 6 | 487 | **256** |
| 7 | 203 | 63 |
| 8 | 541 | 179 |
| 9 | 73 | 51 |
| 10 | **796** | **425** |

Four blocks of ten exceeded the production bound of 200 and the operator accepted each; block 10 is
the first to exceed the total of 600 as well. Three of the four are client blocks, and the reason is
the same each time: a screen is not divisible into halves that both stand alone, and the seam that
exists leaves one half over the bound anyway. That is a fact about the bound meeting a view layer,
and the closing block is where it is worth deciding whether the bound should read a `.tsx` file
differently.

## Tier-2 questions asked, and their answers

1. **Block 2, the budget.** 791 against 600 and 206 against 200. Answered: *"a + gitattributes.
   J'accepte qu'on dépasse exceptionnellement, créer un bloc exprès me semble démesuré."*
   `contract/openapi.json` became a generated artefact for the count.
2. **Block 6, the budget.** 255 against 200, the only seam leaving 204. Answered: *"C'est accepté
   pour les 255 lignes"*.
3. **Block 6, the page cap.** A capped `useInfiniteQuery` drops pages nothing reloads, measured on
   seven pages at a cap of five. Answered: *"c, oui, mais on ne fige pas de solution."* The cap went
   and the backlog holds what is open, naming the constraint without prescribing a remedy.
4. **Block 10, `sourceMediaUrl`.** A pin created from a file on disk names no media, and the input
   DTO required one. Answered: *"Oui, il peut."* It is optional, contract `3.3.0`, and the
   `sourceContextUrl` half of the same problem went to the backlog as a domain change.
5. **Block 10, the budget.** 425 against 200 and 796 against 600. Answered: ship it whole.

Block 1 raised one question without stopping, and it is still open: **`.github/dependabot.yml`
watches `gradle`, `docker` and `github-actions`, and the workspace's locked JavaScript dependencies
are watched by nothing**, in a repository that publishes two SBOMs and an OpenVEX predicate. An
`npm` entry at `/clients` is five lines. ADR 0027 records the gap; block 9 was named as its landing
place and did not take it, so it belongs to the closing block.

## The backlog

The lot filed three items and closed none, the six adjacent ones having been named in specification
section 6 with the reason each stays open:

- **The grid cannot cap the pages it holds** (`P1`), from block 6.
- **A pin cannot be created without a source page URL** (`P1`), from block 10: `sourceContextUrl` is
  non-null in the entity, `not null` in the column and required on the input, so the file entry asks
  for a page URL an image from disk does not have. A domain change, a table rebuild and a contract
  major.
- **The `raw(` calls in production have never been audited** (`P2`), from block 9, which removed the
  twelfth and named the eleven that stay.

The list of what the API serves and the web application does not yet reach, which specification
section 6 says this lot files, is the closing block's to write.

## Next step

The holistic review over `356fe306..<block 10's head>`, then the closing block: its findings, the
backlog reconciled, this document corrected, and the `lot/` tag.

After the lot, the deployment is what turns this into something a user can open: a `Caddyfile` and a
webapp image, which decision B's single public origin needs and which no block here wrote. Everything
the application does rests on the application and the API sharing an origin, and today that is true
in development alone.
