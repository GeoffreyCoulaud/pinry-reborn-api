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

`./gradlew gate` green at this block's tip after `./gradlew --stop`. Blocks 1 to 8 are merged
(pull requests #76 to #86, rebase-only); this block is the last code block and waits for its pull
request. Block 10, the closing block, runs the holistic review over `git diff <lot base>..HEAD`
with the merged blocks included, fixes the findings, and corrects this handoff: each finding's exit,
and how many touched an already merged block (A33). Until it merges, this document is not frozen
(ADR 0020, decision 3).

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

## Not validated

Two of the specification's checks are a reader's, not a test's, and stay so:

- **The oversize body (A19)** was measured once, by a throwaway test with a 3 GiB test heap: a
  multipart body one byte over `32M` answers a bare `413`, a JSON one has its connection closed. No
  test holds it; `quarkus.http.limits.max-body-size` changing would not fail the gate.
- **`StorageLayout` as the only spelling of the three segments (A7)** is a grep run in block 2's
  commit, not a Konsist test, since a string literal is not a declaration Konsist sees.

## Next step

The holistic review (`agents/reviews/holistic.md`) ran on this branch on 2026-09-07, over
`git diff 23b07d10..HEAD`, once this block's gate was green and this handoff written: thirteen
findings, two of them against this block (the backlog's band wording, this document's count and
this section), closed here before the pull request left draft; eleven against merged blocks. Block
10, `fix/p2-debt-closing`, cut from `main` once this block merges, fixes those eleven, corrects this
handoff with each finding's exit and the count against merged blocks, and reconciles
`docs/backlog.md` (A33). The dated documents of the lot freeze when that block merges.
