# Handoff: the repository becomes a monorepo, and one pipeline runs everywhere

Date: 2026-09-09
Specification: `docs/specs/2026-09-08-monorepo.md`
Decisions: `docs/adr/0024-three-projects-share-one-repository.md`,
`docs/adr/0025-the-pipeline-is-written-in-typescript.md`
Tier: Spec, four code blocks, one teammate each (`docs/adr/0023-act-in-a-teammate-per-block.md`).
Base commit: `7d93c31b`. Pull requests: 90 (block 1), 92 (block 2), 93 (block 3), block 4's is this
document's own.

## Current state

Three of the four code blocks are merged. Block 4, the contract guard, is green at its branch tip
(`dagger call gate`, 2m 54s) and waits for the human's reading; the holistic review runs over the
whole lot after it. Nothing here is deployed: the lot changes where code lives and what checks it,
not what the server does.

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

**Block 4, the contract guard** (this block). `quarkus.smallrye-openapi.info-version` is declared
explicitly and the contract now announces `1.0.0` where the build announces `1.0.0-SNAPSHOT`;
`ContractVersionDeclarationTest` pins the two apart. `dagger call contract-guard` runs `oasdiff`
inside the gate with two checks: nothing in `contract/frozen/` may break at all, and a break against
`main` must be admitted by `info.version`. `validate.yml`'s `verify` job checks out with
`fetch-depth: 0`, the guard reading the contract as `origin/main` has it.

## What the pipeline costs

Every number is a measured continuous integration run, not an estimate.

| State | `verify` | `build-image` | Pipeline |
|---|---|---|---|
| Before the lot (`34229652108`) | `lint` 1m 11s and `test` 5m 10s in parallel | the same image build, not recorded on its own | **8m 38s** |
| After block 2 (`34250166952`) | 7m 26s | 2m 33s | **10m 08s** |
| Block 3, both architectures (`34277955208`) | 9m 07s | 10m 06s | **19m 24s** |
| Block 3, one architecture (`34339649929`) | 9m 02s | 4m 30s | **13m 43s**, which is what ships |

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
lot fixes it: the two jobs run on different runners and the Dagger cache volume dies with each. It
is a backlog candidate and not a defect, and its exits are the ones ADR 0024's consequences already
name (a persistent engine, Dagger Cloud, or a runner with storage). None is taken.

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
   makes it an increase. Its rules do parse a prerelease and do fire on one, which is what section 8
   asked; the ordering is the surprise. It bites exactly once, on the commit that leaves the
   prerelease behind, and only if that same commit also breaks the contract. Contract versions are
   plain releases from here on, so the case does not recur.
9. **An empty `contract/frozen/` makes the first `oasdiff` check unfalsifiable.** A wrong path, a
   wrong flag or a swapped base and revision are all green against nothing. Block 4's evidence is a
   throwaway document dropped into the directory, and a counter-check showing that the same pure
   removal reports "No breaking changes to report" with base and revision swapped.

## Not validated

- **That `origin/main` resolves inside the pipeline on a runner.** The contract guard reads the
  previous contract from `origin/main`, falling back to `main`, and `fetch-depth: 0` is there for
  it. A workstation has both refs; that a pull request's detached checkout has the first is proved
  by this block's own continuous integration run and by nothing before it.
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

## The backlog item this lot still owes

Section 6 of the specification binds this lot to file one `Before beta` item: **populate
`contract/frozen/` when the first major becomes still served, and state the support window.** No
block row carried it, blocks 1 and 2 both flagged it as unfiled, and `docs/backlog.md` is in no code
block's file list. **It is the closing block's to file**, and it is lost if Wrap does not.

The lot's other adjacent items keep the exits the specification gave them: **Browser-extension CORS
origin** stays open, ADR 0024 decision 9 widening it rather than closing it, and both it and its new
sibling are the first client lot's to close; **Import follow-ons** is not adjacent; no `P2` item is.

## Tier-2 questions asked

Two, both in the same shape: a measurement reported as a finding, and the operator ruling on it.

- **Block 1**, `api/AGENTS.md`: which file the split delivers it in. Answer: block 1 delivers it,
  and the split follows ADR 0024 decision 2, norms and commands and gate to the ecosystem file.
- **Block 3**, the pipeline going from 10m 08s to 19m 24s. Answer: a pull request tests
  `linux/amd64` alone, the risk of an architecture-specific false negative being low with these
  technologies. `buildx` still ships both on the release path.

Blocks 2 and 4 asked none.

## Next step

**Wrap's closing block**: the holistic review's findings, the backlog item above, and this document
corrected with both. After that, the first client lot, which is what the whole move exists for. It
inherits three things this lot leaves ready and unproven: the contract as an interface artefact with
a version a client can negotiate on, a guard that refuses a version that lies, and a pipeline whose
`gate` takes the contract as an argument the moment `clients/` has a gate of its own.
