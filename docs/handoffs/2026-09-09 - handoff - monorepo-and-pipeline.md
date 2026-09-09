# Handoff: the repository becomes a monorepo, and one pipeline runs everywhere

Date: 2026-09-09
Specification: `docs/specs/2026-09-08-monorepo.md`
Decisions: `docs/adr/0024-three-projects-share-one-repository.md`,
`docs/adr/0025-the-pipeline-is-written-in-typescript.md`
Tier: Spec, four code blocks and Wrap's closing block, one teammate each
(`docs/adr/0023-act-in-a-teammate-per-block.md`).
Base commit: `7d93c31b`. Pull requests: 90 (block 1), 92 (block 2), 93 (block 3), 95 (block 4), and
the closing block's, which carries this document's corrections.

## Current state

The four code blocks are merged. What is open is Wrap's closing block: the holistic review's sixteen
findings, the two backlog items the lot owed, and the corrections below. It is green at its branch
tip (`dagger call gate`, 2m 51s). Nothing here is deployed: the lot changes where code lives and what
checks it, not what the server does.

The repository now holds the Gradle build under `api/`, the generated contract under `contract/`,
and the pipeline under `.dagger/`. `clients/` does not exist and no client is built.

## What was built

**Block 1, the move** (PR 90). The twelve modules, the `Dockerfile`, `config/` and `.idea/` move to
`api/`; `docs/openapi.json` becomes `contract/openapi.json`; `contract/frozen/` is created empty
with its rule in `AGENTS.md`. `AGENTS.md` splits in two: the root file keeps what holds for the
repository, `api/AGENTS.md` takes the API's norms, commands and gate (ADR 0024, decision 2).
Everything else is a rename git detected: 737 of them, and 229 lines of real change.

**Block 2, the gate** (PR 92). `.dagger/` in TypeScript (ADR 0025) with `gate`, `api-gate`,
`contract` and `prose`. `lint` and `test` collapse into one `dagger call gate`. `checkNoLongDashes`
and `checkEvidenceGuard` leave Gradle, their scope being the repository and not the API.
`pre-commit` stops rewriting the working tree and keeps the long-dash refusal alone; `pre-push`
calls the same `dagger call gate` continuous integration calls. `scripts/` disappears with
`generate-openapi.sh`.

**Block 3, the image** (PR 93). `image` and `smoke` as Dagger functions; `build-image` calls them
and keeps its release steps. The smoke test starts what ships and polls `/q/health` from inside the
pipeline, which no workstation could do before. A pull request builds `linux/amd64` alone, on the
operator's ruling; `buildx` still ships both architectures on the release path.

**Block 4, the contract guard** (PR 95). `quarkus.smallrye-openapi.info-version` is declared
explicitly and the contract now announces `1.0.0` where the build announces `1.0.0-SNAPSHOT`;
`ContractVersionDeclarationTest` pins the two apart. `info-title` is declared beside it on the
operator's ruling, the document having published the Gradle module's name until now.
`dagger call contract-guard` runs `oasdiff`
inside the gate with two checks: nothing in `contract/frozen/` may break at all, and a break against
`main` must be admitted by `info.version`. `validate.yml`'s `verify` job checks out with
`fetch-depth: 0`, the guard reading the contract as `origin/main` has it.

**Wrap's closing block** (this document's own). The holistic review's sixteen findings, each with its
exit below. The guard's two halves both stopped at `--fail-on ERR`, which passes seventeen of
oasdiff's breaking rules, so a removed query parameter went green through both; both now fail on
`WARN`, and the version half reads a second `breaking` run against `main` and demands a raised major
for whatever that run reports. `ContractVersionDeclarationTest` refuses a prerelease value. Two
backlog items are filed, `agents/workflow.md`'s green criterion names `dagger call gate`, and the
specification's two falsified statements about the release steps are corrected in place.

## What the pipeline costs

Every number is a measured continuous integration run, not an estimate: `gh run view`, each job from
its start to its end and the pipeline from the run's creation to its last job's end.

| State | `verify` | `build-image` | Pipeline |
|---|---|---|---|
| Block 1 merged, still pre-Dagger (`34229652108`) | `lint` 1m 11s and `test` 5m 10s in parallel | 3m 20s | **8m 41s** |
| After block 2 (`34250166952`) | 7m 26s | 2m 33s | **10m 08s** |
| Block 3, both architectures (`34277955208`) | 9m 07s | 10m 06s | **19m 24s** |
| Block 3, one architecture (`34339649929`) | 9m 02s | 4m 30s | **13m 43s** |
| Block 4, the guard in the gate (`34357626076`) | 9m 24s | 4m 18s | **13m 54s**, which is what ships |

Three readings of that table:

- **The cold cache ADR 0024 accepts costs about ninety seconds on the gate half**, which is what
  losing `gradle/actions/setup-gradle` buys back in exchange for one command everywhere. It changed
  nothing in the decision.
- **The emulated `linux/arm64` half was 5m 33s of every pull request**, to the second. That is the
  ruling block 3 asked for and got.
- **The gate runs strictly more than the old `lint` and `test` did.** `check` pulls in `detektMain`
  and `detektTest`, whose type resolution rules ran on no pull request before this lot
  (specification, section 2, property 4).

**A pull request pays two cold Gradle builds, one in `verify` and one in `image`.** Nothing in this
lot fixes it: the two jobs run on different runners and the Dagger cache volume dies with each. It is
a `P2` item since the closing block, and not a defect; its exits are the ones ADR 0024's consequences
already name (a persistent engine, Dagger Cloud, or a runner with storage). None is taken.

## The holistic review, and what its count does not measure

The review ran over `git diff 7d93c31b..HEAD` with all four code blocks merged, and reported sixteen
findings: one CRITICAL, three MAJOR, twelve MINOR. **Every one of them is against an already merged
block, and that number is therefore not what series costs in rework.**
`docs/adr/0023-act-in-a-teammate-per-block.md` decision 7 has the review dispatched when the last
code block's teammate reports the gate green and the handoff written, its findings against that block
going back to that teammate; the lead dispatched it after block 4 merged instead, so block 4's own
findings had nowhere to go but here. The count is inflated by that, not by the regime, and the
measure decision 7 asks for is unavailable for this lot.

The exits, `docs/adr/0010-review-finding-dispositions.md` giving the four.

| Finding | Exit |
|---|---|
| **CRITICAL.** `--fail-on ERR` passes seventeen of oasdiff's 681 changelog rules, `request-parameter-removed` among them, so a break went green through both halves of the guard | **Fixed here**, on the operator's ruling. Both halves fail on `WARN`, and the version half demands a raised major for any break the `breaking` run reports. Shown red then green on each half. The four sentences that asserted the absolute (`AGENTS.md` twice, ADR 0024 decision 7, specification 4.7) are now true of the code rather than of the prose |
| **MAJOR.** The `Before beta` item section 6 binds the lot to file was still unfiled | **Fixed here**: `docs/backlog.md`, Before beta, pointing at specification 4.3 |
| **MAJOR.** `agents/workflow.md`'s green criterion still said `./gradlew gate`, a command the repository root does not have | **Fixed here**: `dagger call gate`, the criterion every teammate brief rests on |
| **MAJOR.** Nothing refused a prerelease `info.version`, which pitfall 8's disposition rests on | **Fixed here**: one assertion in `ContractVersionDeclarationTest`, shown red at `1.1.0-rc1` |
| **MINOR.** The test resources' comment described the pre-commit generation block 2 deleted | **Fixed here** |
| **MINOR.** `README.md` listed three gate parts where block 4 made four | **Fixed here** |
| **MINOR.** `quarkus-app` was in no table, and the CI section did not name the release call that uses it | **Fixed here**: a third row under The image, and one sentence under CI |
| **MINOR.** `MAIN_REFS`'s `main` fallback silently compared against a local branch | **Fixed here**: `MAIN_REF` is `origin/main` alone |
| **MINOR.** `--max-workers=4` caps the gate on a twelve-core workstation | **Refused, and measured**: the Dagger container sees all twelve cores, and lifting the pin ran the gate's Gradle half in 2m 49s against 2m 47s with it. The pin costs nothing and buys one shape everywhere; the measurement is at the constant |
| **MINOR.** `breaksNoStillServedMajor` did not handle `contract/frozen/` being absent, and its `.gitkeep` was undocumented | **Fixed here**: an absent directory reads as no major still served, and `AGENTS.md` says what keeps the directory in git |
| **MINOR.** The version test reads a contract the same Gradle invocation rewrites | **Accepted limit**, written at the method that reads it. Redundancy inside `dagger call gate`, and the other assertions still discriminate |
| **MINOR.** Specification 4.5 and 7 assert the release YAML is untouched, which block 3 falsified | **Fixed here**, both in the `(Corrected: ...)` form, the specification freezing at this block's merge |
| **MINOR.** Commit `87a310bb`, `chore(agents) allow dagger`, has no colon, and both permission commits have empty bodies | **Named, nothing to fix**: rewriting merged history costs more than the defect. The rule is the format, not a hook: nothing lints a commit message here |
| **MINOR.** The handoff's baseline row was mislabelled and its image build called unrecorded | **Fixed here**: the row is block 1's own head run, inside the lot, its image build 3m 20s and its pipeline 8m 41s by the method the table now states |
| **MINOR.** Two cold Gradle builds per pull request, called a backlog candidate and never filed | **Fixed here**: a `P2` item |
| **MINOR.** Three error paths were never shown red | **Two fixed, one accepted.** `contractOnMain`'s missing ref shown red against `origin/no-such-branch`. The long-dash search's failure branch cannot go red as written and the pitfalls below say why. The smoke test's sixty-second timeout is not shown: an image build and a minute of waiting to exercise a `for` loop |

## Pitfalls, in the order they cost time

1. **A cold Gradle cache exhausts the daemon's metaspace, and Gradle hangs instead of saying so.**
   The symptom is a task marked `FAILED` with no message, no build summary after it, and a process
   at 1% CPU. The cause, read out of the daemon's own log, is `OutOfMemoryError: Metaspace`; the fix
   is `org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=1g` in `api/gradle.properties`, and the bound
   is load-bearing. It cost most of block 2 and is now a gotcha in `api/AGENTS.md`.
2. **Two Gradle invocations must not share one cache volume.** The second dies on
   `Timeout waiting to lock journal cache`. `gate` runs one invocation for both halves, reads the
   contract from that container after its output rather than beside it, and mounts one `Locked`
   volume rather than two, because two locks taken in either order deadlock.
3. **`quarkusBuild` does not rewrite `contract/openapi.json` when the build is up to date.** A
   document edited by hand leaves every source untouched, so the obvious command is one the
   developer runs to no effect. Everything that regenerates the contract names
   `:api-application:quarkusAppPartsBuild --rerun`.
4. **`git diff -- contract/openapi.json` run from `api/` always passes.** The pathspec resolves
   under the current directory, finds no such file, and reports no difference. It cost two false
   greens in block 2. Any check of the contract runs from the repository root.
5. **`asService` ignores the image's `ENTRYPOINT` without `{ useEntrypoint: true }`.** The service
   then has no command at all and the call fails on that rather than on anything about the image.
   The same option is what makes `withExec` run the `oasdiff` binary in block 4.
6. **A named cache volume is not exempt from `dagger core engine local-cache prune`.** Good news,
   the cold path being provable without touching Docker, and a trap: a prune to reclaim disk costs
   the next gate a full dependency download.
7. **The image's build context cannot come from the working tree.** `**/build` is in the pipeline's
   `ignore` list, so the fast jar is not in the directory a function receives. The context is
   assembled from the `Dockerfile` and the Gradle container's output instead.
8. **`oasdiff` reads `1.0.0-SNAPSHOT` to `1.0.0` as a version decrease**, where semver precedence
   makes a prerelease lower than the release it announces. Its rules do parse a prerelease and do
   fire on one, which is what section 8 asked; the ordering is the surprise. It bites exactly once,
   on the commit that leaves the prerelease behind, and only if that same commit also breaks the
   contract. **Exit: an accepted limit of the tool the guard rests on**, and not a backlog item,
   because no work follows from it. It is written where the decision lives: section 8 of the
   specification carries the correction, and the root `AGENTS.md` carries the rule it produces, that
   the contract's version is always a plain release. `ContractVersionDeclarationTest` refuses one
   since the closing block, prose alone having held the rule until then.
9. **An empty `contract/frozen/` makes the first `oasdiff` check unfalsifiable.** A wrong path, a
   wrong flag or a swapped base and revision are all green against nothing. Block 4's evidence is a
   throwaway document dropped into the directory, and a counter-check showing that the same pure
   removal reports "No breaking changes to report" with base and revision swapped.
10. **A container's `expect: ANY` still raises on exit 128 and 129.** Measured at 2, 5, 127, 128, 129
    and 255: every code but those two comes back through `exitCode()`, the two being where a shell
    reports a signal. 128 is also git's code for a fatal error, so `prose`'s "the search itself
    failed" branch never sees one: an invalid pathspec fails the gate through Dagger's own error,
    carrying git's stderr, and the branch guards what is left. It is why that branch cannot be shown
    red, and why deleting it would be wrong.

## Not validated

- **`contract/frozen/` has never held a document outside a demonstration.** The first real one
  arrives when a major becomes still served, and the guard's behaviour with several documents at
  once is exercised by one throwaway file only.
- **`contract/` has no consumer.** The contract, its version and the guard are surfaces whose real
  consumer arrives with the first client lot, which `docs/adr/0018-a-block-is-a-pull-request.md`
  requires be said rather than assumed.
- **The negotiation endpoint is not implemented.** ADR 0024 decision 6 settles what a handshake
  carries; nothing serves one.
- **The `api/vX.Y.Z` tag prefix is not delivered.** `release.yml` triggers on `v*` and
  `validate.yml` derives the image tag with `type=semver,pattern=v{{version}}`; a prefixed tag fires
  neither. It belongs to the lot that ships a second artefact.
- **`oasdiff`'s inline annotations in the "Files changed" tab are not recovered.** They come with
  the official action, and ADR 0024 decision 5 keeps the tool inside Dagger. Emitting `::error`
  commands from its output would recover them at a cost nobody has paid.
- **Dependabot's resolution of both manifests** was only observable after block 1 merged, and
  `.dagger/package.json` is watched by nothing, deliberately (ADR 0025, last consequence).
- **Two of the pipeline's error paths have never fired.** The long-dash search's failure branch
  cannot, for the reason pitfall 10 gives, and the smoke test's sixty-second timeout was not
  exercised. `contractOnMain`'s missing ref was, in the closing block.
- **The guard has never refused a real break.** Every red above is a document broken on purpose and
  reverted; no contract change in this repository has yet needed a major.

## The backlog

The item section 6 of the specification binds this lot to file is filed by the closing block:
**populate `contract/frozen/` when the first major becomes still served, and state the support
window**, in `Before beta`, pointing at specification 4.3. The closing block files a second, in
`P2`: **a pull request pays two cold Gradle builds**, which had been named a candidate here and
nowhere else.

The lot's other adjacent items keep the exits the specification gave them: **Browser-extension CORS
origin** stays open, ADR 0024 decision 9 widening it rather than closing it, and both it and its new
sibling are the first client lot's to close; **Import follow-ons** is not adjacent. No item was
closed by a block, so there is nothing to reconcile beyond the two additions.

## Tier-2 questions asked

Three, one per block except block 2 and the closing block, which asked none. The closing block's one
judgement call, the worker pin, was settled by measuring it rather than by asking.

- **Block 1**, `api/AGENTS.md`: which file the split delivers it in. Answer: block 1 delivers it,
  and the split follows ADR 0024 decision 2, norms and commands and gate to the ecosystem file.
- **Block 3**, the pipeline going from 10m 08s to 19m 24s. Answer: a pull request tests
  `linux/amd64` alone, the risk of an architecture-specific false negative being low with these
  technologies. `buildx` still ships both on the release path.
- **Block 4**, `info.title` publishing the Gradle module's name, `api-application API`, from the same
  fallback the block was removing for the version. Answer: declare it too, as `Pinry Reborn API`. The
  specification's review had raised the version and not the title, and the operator recorded that as
  the specification's omission rather than a decision, which is worth carrying: an argument from a
  review's silence is not available.

## Next step

**Wrap's second half**, once the closing block merges: the lot needs no tag, and the report of what
was done, its friction points and every tier-2 question with its answer is the input to Improve. The
one thing that report has to carry is the dispatch error above: the holistic review ran after block 4
merged, and ADR 0023 decision 7's measure is corrupted for this lot because of it.

After that, the first client lot, which is what the whole move exists for. It
inherits three things this lot leaves ready and unproven: the contract as an interface artefact with
a version a client can negotiate on, a guard that refuses a version that lies, and a pipeline whose
`gate` takes the contract as an argument the moment `clients/` has a gate of its own.
