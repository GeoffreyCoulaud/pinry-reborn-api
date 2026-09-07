# Handoff: the P2 band empties

Date: 2026-09-07 (the lot's specification is dated 2026-09-05, and this file carries its name)
Branch of this block: `fix/p2-debt-lost-lease`, the eleventh of twelve pull requests, the closing block
being the twelfth
Specification: `docs/specs/2026-09-05-p2-debt-elimination.md`
Decisions: `docs/adr/0020-two-reviews-and-an-inline-act.md` (the regime), `docs/adr/0021-framework-refusals-share-the-problem-format.md`,
`docs/adr/0022-a-lost-lease-is-an-exception.md`
Tier: Spec. One specification review (closed, spec section 11), one holistic review, run by the
closing block over this branch with this handoff in its diff (ADR 0020, decisions 1 and 4).

## Current state

`./gradlew gate` green at the closing block's tip. Blocks 1 to 9 are merged (pull requests #76 to
#87, rebase-only); block 10, `fix/p2-debt-closing`, closes the holistic review's findings (the table
below) and is the lot's last pull request. Until it merges, this document is not frozen (ADR 0020,
decision 3). *(Corrected in block 10: written in block 9 as "this block is the last code block and
waits for its pull request".)*

The backlog's `P2` band holds nothing (A32). The twenty-three items of the specification's section
2 left by three exits: fixed (nineteen), accepted limit (one: the rule's construction blindness,
`docs/backlog.md` Known limits), refused (two, below).

## What was built, one pull request per block

| Block | Pull request | What landed |
|---|---|---|
| 1 `docs/p2-debt-regime` | #76 | ADR 0020, `agents/reviews/spec.md` and `holistic.md`, the living documents, ADR status lines, the backlog's four bands, the spec |
| 2 `fix/p2-debt-sweep-names` | #77 | `ReapUserDataExports`, `ReapUserDataImports`, `StorageLayout` in the domain, the import sweep under `safeReap` |
| 3 `fix/p2-debt-paged-sweeps` | #78 | Every sweep selection paged by `id` (`SweepPages`, `pageByIdAfter`), `reapExpired` bounded, `imports.sweep_batch_size` |
| 4 `test/p2-debt-export-fixtures` | #79 | One fixtures base, a fake-store sibling and a mock-store sibling |
| 5 `fix/p2-debt-openapi` | #81 | `@APIResponse` on the export endpoints, the tag fold's `@Operation`, the chunk offset's `@DefaultValue` |
| 6 `fix/p2-debt-problem-responses` | #82 | `ProblemResponses` object, `FrameworkErrorCode` with the five existing codes |
| 6b `fix/p2-debt-error-format` | #83 | ADR 0021, the eight-mapper family, the built-in mapper disabled, `TestFailuresResource`, nine integration cases |
| 6c `fix/p2-debt-image-state-read` | #84 | `ResolvePinImageState` reads the image and its replacement in one transaction (adjacent, tier 2) |
| 7 `fix/p2-debt-fences` | #85 | `Fences.kt`, the four delegations, `PinAccess` and `BoardAccess`, the three sites fenced, the rule's boundary names |
| 8 `fix/p2-debt-fence-rule` | #86 | `RowMergedOutsideTransaction` over every use case, `^save`, four inlinings, two transaction moves, the KDoc's three limits |
| 9 `fix/p2-debt-lost-lease` | this one | ADR 0022, `TaskLeaseLostException`, the `Abandoned` outcome, both nets rethrowing, this handoff, the band emptied |

Three departures from the specification's block table, each recorded in it in the `(Corrected: ...)`
form: block 6 split in two (669 lines against 600), block 6c was added for an adjacent defect the
operator chose to take as its own block, and the rule's boundary names moved from block 8 to block 7.
Blocks 6b and 8 stayed over the 200 production lines with the one-line reason the rule allows.

## The two refusals, with their reasons

- **Item 8, `TaskQueueBootIntegrationTest` counts every row in `tasks`** (D7). The two-line fix,
  counting its own kind, was on the table and refused: the mechanism by which another class's row
  reached that count was never reproduced, and repairing an unexplained symptom hides the next one.
  The item leaves the backlog with this paragraph as its record.
- **Item 23, measure what review costs and what it returns** (D2). The decision the measurement
  would inform, which reviews to keep, was taken by D1 without it, as ADR 0014, 0018 and 0019 had
  changed the regime without it before. ADR 0020's consequences say so as a decision rather than
  leaving the item to the next lot. The item leaves the backlog with this paragraph as its record.

## Pitfalls, in the order they cost time

1. **detekt's form rules cost a gate cycle or two per block**: `MaxLineLength` at 120 on KDoc lines,
   `CommentCarriesDocumentation` at four lines including delimiters, `TooManyFunctions`,
   `UseCheckOrError`, `SwallowedException`, and the pair `InstanceOfCheckForException` /
   `RethrowCaughtException`, which together leave one shape for a net that lets one exception through:
   a dedicated rethrow arm, suppressed with its reason (ADR 0022, decision 3). Run
   `:module:detekt` before the gate when a block adds comments.
2. **The budget is measured after the fact and should not be**: block 6 was written whole, measured
   669 lines and 376 in production, and split after its gate was green, costing a WIP commit, a
   second branch and a rebase with conflicts. `git diff main --numstat` summed by `awk`, excluding
   `docs/adr`, `docs/specs` and `docs/handoffs`, takes a second; take it right after the first green run.
3. **A test run rewrote `docs/openapi.json`** once a test-only JAX-RS resource existed:
   `quarkus.smallrye-openapi.store-schema-directory` applies in test mode, which indexes `src/test`.
   The test `application.properties` empties it; the hook and CI generate the file from the
   production build alone (ADR 0021, decision 6).
4. **The Gradle daemon in Claude's shell ran JDK 21** and detekt failed on class file version 69:
   `export JAVA_HOME=~/.sdkman/candidates/java/25-tem` before every `./gradlew` and `git commit`
   (the pre-commit hook runs Gradle), and `./gradlew --stop` after a detekt rule changes.
5. **The pre-commit hook regenerates `docs/openapi.json` and exits non-zero when it changed**: re-run
   the same commit. A red commit that does not compile takes `--no-verify` and says so.
6. **A CI failure in a test the block never touched was a real defect**: pull request #83's first
   run read a torn pair in `ModeBImageHostingIntegrationTest`, the swap's transaction landing between
   two autocommit reads of `ResolvePinImageState`. The rerun passed, three local runs passed, and the
   code reading settled it; block 6c fixed it with a fake runner that models the single connection.
   A flake in an untouched test is a question, not a rerun.
7. **The Quarkus sources jars in the Gradle cache answered what the documentation did not**: the
   Jackson reader's wrapping, the routing handler's `406` condition, the mapper resolution's real
   order. `find ~/.gradle/caches -name "*-sources.jar"` and `unzip` into the scratch directory.
8. **MockK's unnecessary-stubbing check fails a case whose failure path stops early**: a heartbeat that
   throws before the pin walk leaves every pin-walk stub unused. Stub what runs, not what the
   neighbouring case stubs.

## The holistic review's findings, and the closing block (block 10, `fix/p2-debt-closing`)

Thirteen findings, two MAJOR and eleven MINOR; **eleven of the thirteen touched an already merged
block**, which is what series cost this lot in rework (ADR 0020, decision 4). Each with its exit:

| # | Severity | Against | Finding | Exit |
|---|---|---|---|---|
| 1 | MAJOR | block 5, #81 | `@DefaultValue("0")` on a primitive `Long` made `offset` `required: true` in `docs/openapi.json`, a breaking change 4.4 promised not to make | Fixed: `@Parameter(required = false)`; A16 gains the `required` check |
| 2 | MAJOR | block 6b, #83 | `JsonProcessingException` is an `IOException`, so `InvalidDefinitionException` and serialisation failures landed on the `IOException` row at DEBUG, and so did a disk failure | Fixed: the row logs by whose failure it is (ERROR, DEBUG, WARN); ADR 0021 corrected |
| 3 | MINOR | block 3, #78 | `SweepPages` loops until an empty page with no guard; a selection that forgets its key spins forever | Fixed: `MAX_PAGES` stops it loudly; the full-scan cost of a refusing store is in spec section 8 |
| 4 | MINOR | block 8, #86 | `PinryRuleSetProviderTest`'s KDoc still described the rule's import-only reach | Fixed |
| 5 | MINOR | block 8, #86 | `agents/engineering.md` pointed at `UserDataExportRequester.kt:58`, a line block 8 moved | Fixed: named by function |
| 6 | MINOR | block 9, #87 | The backlog's `P2` band carried a shipped log and line 15 a self-dating count | Fixed in block 9 |
| 7 | MINOR | block 9, #87 | This handoff said "of eleven pull requests" and lacked "Not validated" | Fixed in block 9 |
| 8 | MINOR | block 6b, #83 | `TestFailuresResource`'s KDoc named "spec 4.5" without the file | Fixed |
| 9 | MINOR | block 6b, #83 | The conversion-failure detail of `UNKNOWN_ROUTE` was never read off the wire | Fixed: one integration case, `GET /api/v1/pins/not-a-uuid` |
| 10 | MINOR | block 3, #78, process | The red commit's failing run was a compile error; A10 and A12 were never observed failing | Recorded: block 10's commit carries both mutation runs (first page only: two cases fail; `setMaxRows` removed: one case fails) |
| 11 | MINOR | blocks 6b and 7, process | Five unit cases arrived in green commits, written after the code they cover | Recorded here: the branches are spec-demanded, nothing deleted; a coverage gap found at the gate goes back through a red commit from now on |
| 12 | MINOR | block 5, #81 | A third private `PROBLEM_JSON` literal beside `ProblemResponses.PROBLEM_JSON_MEDIA_TYPE` | Fixed: the three controllers import the constant under that name |
| 13 | MINOR | block 1, #76 | Spec section 8's observable on `EbeanTaskQueue.kt` was made false by block 3 | Fixed: `(Corrected: ...)` in section 8 |

Nothing was found under criterion 1 (layering) or 6 (unrequested code). The closing block's own
tests are coverage, not red: the `IOException` row's observable is a log level no test reads, the
conversion detail already answered before its case, and a red run of the page cap against the old
loop would not terminate.

## Not validated

Two of the specification's checks are a reader's, not a test's, and stay so:

- **The oversize body (A19)** was measured once, by a throwaway test with a 3 GiB test heap: a
  multipart body one byte over `32M` answers a bare `413`, a JSON one has its connection closed. No
  test holds it; `quarkus.http.limits.max-body-size` changing would not fail the gate.
- **`StorageLayout` as the only spelling of the three segments (A7)** is a grep run in block 2's
  commit, not a Konsist test, since a string literal is not a declaration Konsist sees.

## Next step

The holistic review (`agents/reviews/holistic.md`) ran on block 9's branch on 2026-09-07, over
`git diff 23b07d10..HEAD`, once that block's gate was green and this handoff written: thirteen
findings, two against block 9, closed there before its pull request left draft; eleven against
merged blocks, closed by block 10, the closing block, whose diff corrects this document and carries
the table above (A33). `docs/backlog.md`'s `P2` band holds nothing. The lot ends when block 10
merges, and its dated documents freeze then (ADR 0020, decision 3). Nothing is handed to a next lot.
