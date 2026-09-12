# Handoff: the web application arrives, and the API grows the four things it needed

Date: 2026-09-11
Specification: `docs/specs/2026-09-10-web-application.md`
Decisions: `docs/adr/0026-one-session-two-transports.md`,
`docs/adr/0027-the-web-application-stack.md`
Tier: Spec, ten code blocks and Wrap's closing work in two, one teammate each
(`docs/adr/0023-act-in-a-teammate-per-block.md`).
Base commit: `356fe306`. Pull requests: 98 (block 1), 99 (2), 100 (3), 101 (4), 102 (5), 103 (6),
104 (7), 105 (8), 106 (9), 107 (10), 108 (11), and block 12's, which carries this document's
corrections.

## Current state

The eleven code blocks are merged. What is open is block 12, the second half of the closing work and
this document's own pull request: the holistic review's document findings, the backlog reconciled,
and the corrections below. After it merges the lot is tagged and the specification freezes.

**A user can now sign up, sign in, see their pins in a grid that does not jump, create a pin from an
address or from a file on disk, and watch a download run or fail.** Nothing else: no boards, no
tags, no search, no recycle bin, no account screens, no import or export, and no extension. Section
7 of the specification names each and the observable that would show one had slipped in.

Nothing here is deployed. The bundle is built by `pnpm build` and by nothing else: no reverse proxy
configuration and no webapp image exists, and the deployment that makes the application same origin
with the API is the next lot's (see *Next step*).

The contract went from `1.0.0` to `4.0.0` across six blocks, three of them breaking: block 2 for the
required `transport`, block 5 for the opaque cursor and the declared enumerations, block 11 for the
declared `201` that removed the `200` SmallRye had derived. `contract/frozen/` is still empty, which
is what let the three breaks land: the alpha's policy is the README's.

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

**Block 10, creating a pin and the task centre** (PR 107, contract `3.3.0`). `/pins/new`
carries both entries on one screen: an address goes to `POST /api/v1/pins` then `PUT
/api/v1/pins/{pinId}/image` as JSON, answered `202`, and the tile stays out of the grid until the
download settles; a file goes to the same `PUT` as multipart and its tile enters at once. The header
carries the task centre, which polls while the server still has work and rereads the grid when a
download settles, and a failed row offers the address again, a file from disk, or forgetting it.
`PinCreationInputDto.sourceMediaUrl` becomes optional, without which the file entry cannot create a
pin at all.

**Block 11, the review's code findings** (PR 108, contract `4.0.0`). The file recourse no longer
deletes a row `SetPinImage` has already cleared; the task centre announces each mutation the way the
creation screen does; a transient failure no longer signs the user out; a failure reason is read
through the catalogue in the reader's language; `lib/`'s three hand-typed contract types derive from
the generated schema; the tile reads `renditionSizes.small` from the handshake instead of a constant;
`POST /api/v1/pins` declares the `201` it answers, with a test comparing every route's built success
codes against the declared ones; one deletion reads one row through `findByAuthorAndPin`; and the
page cap's and the pager's assertions are in place. Declaring the `201` removes the derived `200`, so
the contract is `4.0.0`.

**Block 12, the documents** (this pull request). The review's document findings, each with its exit
below; the backlog reconciled; `.github/dependabot.yml` watching the pnpm workspace at last; and this
document. No production code but the theme comment and the Dependabot entry.

## The holistic review, and what its count does not measure

The review ran over `git diff 356fe306..ac5b1412`, block 10's head, and reported thirty-one findings:
one CRITICAL, fifteen MAJOR and fifteen MINOR. **All thirty-one are against an already merged block,
so that number is not what series costs in rework.** `docs/adr/0023-act-in-a-teammate-per-block.md`
has the review dispatched when the last code block's teammate reports the gate green and the handoff
written, its findings against that block going back to that teammate; the operator chose to merge
block 10 first, deliberately, so no finding had a live teammate to go back to and both halves of the
closing work paid for all of them. The measure decision 7 asks for is unavailable for this lot, as it
was for the last one.

The exits, `docs/adr/0010-review-finding-dispositions.md` giving the four. The code half keeps the
numbers the review gave it, which PR 108 answers one by one; the document half is grouped by subject,
several of the review's rows being one finding over a list of counts.

| Finding | Exit |
|---|---|
| **1** CRITICAL. The file recourse deleted a download row `SetPinImage` had already cleared, so the `404` rejected the mutation and neither the grid nor the centre reread | **Fixed in block 11.** The journey uploads through the centre's file input and asserts the tile |
| **2** to **9** MAJOR. An alert per mutation; a session no transient failure ends; the reason read through the catalogue; `lib/`'s hand-typed contract types; the tile on the handshake's rendition sizes; the `201` of pin creation with the guard over every route; one row read for one deletion; the two shipped behaviours no assertion covered | **Fixed in block 11**, each with the mutation that holds it. The `201` cost a contract major, which is the tier-2 question below |
| **10** MAJOR. Every settled download refetches the whole accumulated catalogue, sequentially | **A backlog item** (`P1`). Block 11 dropped it on its budget, the real fix being to write the settled pin into the cached pages; the cost stands and the item names it |
| **11** MINOR, three of them. `ContractConfig` untyped, a comment that said less than it meant, `pnpm-workspace.yaml`'s stale line | **Fixed in block 11** |
| MAJOR. `docs/adr/0024` read `Status: Accepted` while ADRs 0026 and 0027 each revise one of its decisions, and `docs/specs/2026-07-21-cors.md` still asserted the `Authorization` header "only and will stay so" and "no cookies in play" | **Fixed here**, and this is the only change a dated document takes: the superseded markers, on the precedent of `docs/adr/0006`, with ADR 0026 naming the CORS specification back |
| MAJOR. `README.md` did not know the web application exists: four gate parts where there are five, and no `clients/` anywhere | **Fixed here.** The workspace in the opening, the fifth gate part, and how to start the application against a local API |
| MAJOR. Four living norm documents described the pre-lot world, `git log 356fe306..2b7071bf -- agents/` being empty | **Fixed here.** `agents/engineering.md` carries two schemes, the filter that stamps both and the required `transport`; `agents/writing.md` lists three `AGENTS.md`; `agents/reviews/holistic.md` names both coverage perimeters; `agents/workflow.md` carries the 200-line production sub-bound and the three generated exclusions |
| MAJOR. `clients/AGENTS.md` said "the seven steps below" and six followed | **Fixed here**: the install two bullets above is the seventh |
| MAJOR. Question T's manual theme switch was dropped, and the only record was a CSS comment promising a later block | **Fixed here as a record, and filed.** Question T carries the correction, the comment points at the item, and the switch is a `P1` item. The operator asked for it, so out of scope would have lost it |
| MAJOR. The backlog's three new items ran six to seven lines each against the two-line rule | **Fixed here.** Symptom, location, pointer; the argument stays in the specification and here, which is what the rule buys |
| MAJOR. "Twelve remain across eight files" was wrong, and three documents of one lot gave three numbers | **Fixed here.** Thirteen across nine, `git grep -c "raw(" -- 'api/**/src/main/kotlin/**'`, and the same commands at `356fe306` answer thirteen and nine too: the lot removed no pre-existing call, block 9 removed the one block 8 introduced. The specification, this document and the backlog now agree |
| MINOR. The specification's stale counts: six gate steps for seven, nine journeys for ten, six repository methods for eight, ten blocks for twelve, an optimistic mutation that exists nowhere, and a budget compared against 800 | **Fixed here**, each in the `(Corrected: ...)` form at the sentence it corrects |
| MINOR. The polling departure was recorded in `downloads.ts`'s comment alone | **Fixed here**: specification 4.8 carries it, polling waiting on a `PENDING` row and not on a non-empty list |
| MINOR. Two answers about a recycled pin's download row were unstated: `findByPinIds` filters no pin state, so `/pins/recycled` reports what the task centre hides | **An accepted limit, written where the decision lives**: specification 4.4. The bin is about the pin, and hiding the image state there would show a recycled pin as having none |
| MINOR. `clients/pnpm-workspace.yaml`'s stale comment about empty packages | **Fixed in block 11**, verified gone here |

Four things decided during the lot had landed nowhere, and they are this block's too.
**`.github/dependabot.yml` now watches the pnpm workspace**, an `npm` entry on `/clients`: block 1
raised it, ADR 0027 records it as a consequence, block 9 was named as its landing place and did not
take it. **The two cold Gradle builds item is `P1`** on the measurement below. **Block 3's exemption
line** is in specification section 5. **Block 2's production figure** is measured at 232 and not the
206 block 10's report carried, the pair 791 and 206 being the first green run's, before the SmallRye
fix added 73 lines.

## What a pull request waited for

Measured with `gh run list`, each figure the whole pipeline from the run's creation to its last job's
end, on the eleven pull requests of this lot: eight ran between 12m 05s and 15m 04s, and three ran
30m 34s, 36m 14s and 39m 17s on a slow runner, for about three hours and forty minutes of waiting
across the lot. Inside a normal run, `validate / verify` takes about 9m 15s and `validate /
build-image` about 4m 45s, and `build-image` declares `needs: [verify]`, so the two are in series and
a pull request waits about fourteen minutes for jobs that hold about fourteen minutes of work between
them.

This block's own run is the twelfth and it is the longest: `validate / verify` 9m 38s, `validate /
build-image` 4m 08s, `validate / gate` 5s, and 19m 17s from the run's creation to its last job's end,
five of those minutes being the final job waiting for a runner. Two jobs of work, three waits.

**The backlog item gains a fourth exit ADR 0024's consequences do not name**, because theirs are
about the cache and this one is about the job graph. `build-image` waits on `verify` for two reasons:
not spending runner minutes on a failing pull request, and not publishing an unvalidated image to
GHCR. The second holds on the release path alone. Parallelising the two jobs on pull requests only
removes about a third of the wait for a few lines of YAML, which is why the item moved to `P1`.

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
- **React Aria's popover puts the rest of the page out of the accessibility tree**
  (`ariaHideOutside`), so no `*ByRole` query reaches the grid while the task centre is open. Closing
  it on an Escape keystroke passed on the workstation and lost the race in the gate's container; the
  journey reads the tile by its alt text instead.
- **A loaded container is slower than the workstation, and a journey's timeout is not a machine
  measurement.** Two journeys waited one second where the gate needed 1.1, and a red gate is what
  said so. Wait for the application's own state, never for a duration.
- **A widened structural test finds callers no module you touched names.**
  `HandshakeControllerTest` passed the contract version as a `String` and only the gate's whole-build
  compile caught it.
- **A teammate's `SendMessage` to `main` can be lost.** Block 9's stop message was sent and never
  delivered; the lead found the open pull request twenty-five minutes later by inspecting the branch.
  If a report draws no answer within a few minutes, send it again.
- **A background command's completion does not re-invoke an idle teammate**
  (`docs/adr/0023-act-in-a-teammate-per-block.md`, consequences). Block 11 stopped to wait for a
  monitor to wake it with its gate's verdict, which cannot happen, and the lead woke it by watching
  the process. Every teammate of this lot was told, and one still hit it: run the gate and every wait
  in the foreground.

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
- **Relative image URLs are verified in the response and exercised nowhere.** Every URL the API emits
  starts with `/api/v1/`, asserted on the API side; that they resolve for a browser is the reverse
  proxy's job, and the proxy is not delivered.
- **The bidirectional page reload has no consumer.** `PaginationOutputDto.previousCursor` and
  `CursorDirectionDto.BACKWARD` are served and nothing asks for them, the page cap having gone.
- **Dependabot against pnpm 12.** The workspace pins `pnpm@12.3.4` in `packageManager`, and
  Dependabot's supported list stops at 11; `pnpm-lock.yaml` declares `lockfileVersion: 9.0`, which it
  reads as pnpm 10. The first weekly run is what says whether the entry resolves the workspace.

## The lot against ADR 0018's budget

| Block | Total | Production |
|---|---|---|
| 1 | 503 | 97 (181 counting the pipeline and the hook) |
| 2 | **778** | **232** |
| 3 | 594 | **262** |
| 4 | 400 | 111 |
| 5 | 221 | 51 |
| 6 | 487 | **256** |
| 7 | 203 | 63 |
| 8 | 541 | 179 |
| 9 | 73 | 51 |
| 10 | **796** | **425** |
| 11 | 551 | 165 |
| 12 | 100 | 14 |

Block 2's row is measured from its merged range, `git diff --numstat aca1ffe0..47e232c5` excluding the
dated documents and the generated contract; the 791 and 206 its tier-2 question carried were the
first green run's, before the SmallRye fix added 73 lines, and 206 travelled into this table by
mistake.

Four blocks of twelve exceeded the production bound of 200 and the operator accepted each. Two of
them, 2 and 10, exceeded the total of 600 as well, block 10 by the wider margin. Three of the four
are client blocks, and the reason is the same each time: a screen is not divisible into halves that
both stand alone, and the seam that exists leaves one half over the bound anyway.

**Whether the bound should read a `.tsx` file differently is left open**, and deliberately: it changes
`docs/adr/0018-a-block-is-a-pull-request.md`, which is a decision of its own and not a document
correction. What this lot measured is the input to it. A screen's production line count is dominated
by markup that a reader skims rather than verifies, so the honest shapes are a separate bound per
ecosystem or a bound that counts statements rather than lines; both need an ADR, and neither belongs
to a closing block.

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
6. **Block 11, the contract's version.** Declaring the `201` of `POST /api/v1/pins` removes the `200`
   SmallRye derived from the return type, which `oasdiff` rates an error, so the additive bump the
   review expected was refused by `contract-guard`. Answered: *"a"*, the major is accepted. The
   contract is `4.0.0`.

Block 12 asked none.

Block 1 raised one question without stopping, and block 12 closed it: **`.github/dependabot.yml` now
watches the pnpm workspace**, an `npm` entry on `/clients`, in a repository that publishes two
software bill of materials documents and an OpenVEX predicate. ADR 0027 records the gap; block 9 was
named as its landing place and did not take it.

## The backlog

The lot filed the items below and closed none, the six adjacent ones having been named in
specification section 6 with the reason each stays open. The three filed during the lot were
rewritten at the two lines the rule allows, their argument staying in the specification and here.

From the code blocks:

- **The grid cannot cap the pages it holds** (`P1`), from block 6.
- **A pin cannot be created without a source page URL** (`P1`), from block 10.
- **The `raw(` calls in production have never been audited** (`P2`), from block 9, re-measured at
  thirteen calls across nine files.

From the closing work:

- **Every settled download refetches the whole accumulated catalogue, sequentially** (`P1`), the
  review's finding 10, which block 11 dropped on its budget.
- **Two wire names and two declarations the contract got wrong while breaking was free** (`P1`), and
  **`POST /api/v1/sessions` declares no `401` although it answers one** (`P1`), which is the other
  half: `MeImageDownloadController` is now the only controller declaring `application/problem+json`
  with `ProblemDetail`.
- **Two surfaces of this lot are half consumed** (`P1`): `Session.renewAfter` reaches the client and
  nothing reads it, and `/pins/new` carries no task centre.
- **No manual theme switch** (`P1`), which is question T's answer, unbuilt.
- **The download list is unbounded, unpaginated and polled every second, and nothing sweeps a failed
  row** (`P2`).
- **`foreign_keys` is off, so every declared key is unenforced** (`P2`), and `1.22.sql` spent a table
  rebuild on a constraint nothing checks.
- **What the API serves and the web application does not reach yet** (Features), the plain list
  specification section 6 binds this lot to file.

The two cold Gradle builds item moved from `P2` to `P1` and gained the fourth exit above.

## Next step

The lot's `lot/` tag on this pull request's merge, which is Wrap's second half, and the report to the
operator.

After the lot, the deployment is what turns this into something a user can open: a `Caddyfile` and a
webapp image, which decision B's single public origin needs and which no block here wrote. Everything
the application does rests on the application and the API sharing an origin, and today that is true
in development alone.
