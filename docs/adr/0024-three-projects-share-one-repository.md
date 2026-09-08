# 0024. Three projects share one repository, and one pipeline runs everywhere

Status: Accepted
Date: 2026-09-08
Specification: `docs/specs/2026-09-08-monorepo.md`
Related: `docs/adr/0018-a-block-is-a-pull-request.md` (the block budget block 1 is measured
against), `docs/adr/0010-review-finding-dispositions.md`, `docs/adr/0009-unique-index-named-outcomes.md`
(the guard grammar decision 7 follows: a rule that refuses, not a convention that is reread).

## Context

**The repository holds one project and the product needs three.** The API server exists; a web
application and a browser extension do not. The backlog already anticipates them: its `P1` band is
named "Client ergonomics (needed for the web UI and browser extension)" and holds an item that
waits for the extension to have a stable identifier.

**The operator's reason for one repository runs in both directions.** A contract change and its
consumers travel in the same pull request; and working on the web interface of an alpha API
reveals API changes that should land in that same pull request. Convenience of administration and
sharing the process apparatus were both available with three repositories and were not the reason.

**The gate and continuous integration already diverge, by construction.** `validate.yml` carries
its own admission next to the check that pays for it:

> CI reproduces the gate's parts rather than calling `gate`, so a check added there is not in CI
> until it is added here too.

`AGENTS.md` repeats it under CI. The divergence has one cause: continuous integration enumerates
*checks*, and that list changes every time a check is added. A second ecosystem doubles the list
and doubles the divergence.

**Three facts about the code decided three of the questions below**, and each was read rather than
assumed:

| Read | Command | Result |
|---|---|---|
| No domain visibility | `grep -rn -i "visibility\|isPublic" api-domain/src/main/kotlin` | Nothing. `Board` and `Pin` have no public or private state, so no page has a reason to be indexed |
| No feature toggle | Every `@ConfigMapping` under `api-presentation-quarkus` and `api-worker-quarkus` | All tuning, no `enabled` flag. What a client cannot guess is *limits*, not capabilities |
| No Testcontainers | `grep -rn -i "testcontainer\|docker" --include="*.kt"` | Only `ImportDataDirectoryImageTest` and `ExportDataDirectoryImageTest`, which read the `Dockerfile` as text. Nothing runs Docker inside a test |

**Two repository-wide checks live in Gradle by accident of geography.** `checkNoLongDashes` runs
`git ls-files` from `rootDir` and `checkEvidenceGuard` runs the guard's tests from `rootDir` with
`python3`. They cover the whole repository today only because Gradle's root and the repository's
root are the same directory. Moving the build under `api/` separates them, which is what exposed
the accident.

### What was rejected

- **Nx with `@nx/gradle`**, the lightest tool that models Gradle modules and JavaScript packages in
  one graph. It requires `nx.json` and `package.json` at the repository root and a
  `dev.nx.gradle.project-graph` plugin inside the build files, which undoes decision 1. The
  operator also refused the pull toward its cloud offering. It answers a problem of scale that one
  Gradle build and two applications do not have.
- **Bazel and Buck2.** A real graph across both ecosystems, at the price of replacing Gradle. The
  Quarkus augmentation and Ebean's bytecode enhancement are Gradle plugins; reproducing them is a
  project, not a migration.
- **Pants.** Its Kotlin backend compiles, tests and lints on the JVM; JavaScript and TypeScript are
  not in its supported set. It covers the half that already works.
- **Path filters alone** (`dorny/paths-filter` and equivalents). They break exactly where projects
  share code, which is the one edge this repository has: `api` produces the contract, `clients`
  consumes it.
- **A declarative manifest of gate parts, expanded into a job matrix with waves derived by
  topological sort.** Proposed here first and refused as a hand-written re-implementation of a task
  graph scheduler, non standard and hard to read. The refusal was correct.

## Decision

1. **The repository holds two ecosystem roots, and neither is the repository root.** `api/` holds
   the Gradle build, its twelve modules, its `Dockerfile` and its `.idea/`. `clients/` holds the
   JavaScript workspace: `clients/apps/webapp`, `clients/apps/extension`, `clients/packages/*`. The
   repository root holds no `package.json` and no `settings.gradle.kts`. A workspace convention
   belongs to the tool that declares it, and Gradle already declares one; giving each ecosystem its
   own root is the only layout where neither lies, and where each editor opens exactly one
   directory. The shared packages live inside the perimeter that consumes them, not beside a build
   that never will.

2. **The process is shared and the technical norms are not.** The root `AGENTS.md` keeps the
   phases, tiers, reviews, backlog rules and writing rules; `docs/` stays one set of series. Each
   ecosystem root gets its own `AGENTS.md` for its norms, its commands and its gate. Phases and
   traceability do not depend on a language. A branch coverage bound does.

3. **The contract is an interface artefact, not documentation.** `contract/openapi.json` at the
   repository root, generated, committed, never edited by hand, with `contract/frozen/` holding one
   document per contract major still served. It belongs to neither ecosystem: `api/` produces it,
   `clients/` consumes it. `docs/` is governed by a prose regime (`agents/writing.md`) and a
   generated schema was always a category error there.

4. **The generated TypeScript client is not committed; the contract is.** The client is produced at
   install time from `contract/openapi.json`, so `clients/` builds with no JVM and no prior Gradle
   run. What makes a contract change visible in a pull request is the diff of the contract, which
   is already the mechanism the `pre-commit` hook was built around.

5. **Dagger owns the pipeline, and its perimeter is what has a meaning locally.** The gate, the
   image build and the smoke test run as Dagger functions, called by the same command on a
   workstation and in continuous integration. The release path stays in the workflow file: GHCR,
   keyless cosign attestations, the CycloneDX and Syft SBOMs, the OpenVEX predicate. Those need
   GitHub's OIDC identity and a registry, never run locally, and would buy nothing from being
   identical everywhere. The smoke test crosses the line in the other direction and is the strongest
   case for the whole decision: `validate.yml` records that a datasource declaration Ebean rejects
   survived a lot and a half of green builds because nothing but that step ever started what ships,
   and that step could not be run before pushing.

6. **The three artefacts version independently, and a client negotiates at startup.** The handshake
   carries the contract major and minor, and the deployment's limits. It carries no capability
   registry: no configuration key in this repository turns a feature on or off, so a registry would
   describe nothing. A limit is different: `ImportsConfig.maxChunkBytes` already carries the comment
   "This key records the size a client is told to send", which admits the need. Tags gain a prefix,
   `api/vX.Y.Z` and one series per project.

7. **The gate refuses an undeclared breaking change and a version that lies.** `oasdiff` compares
   the contract and fails on a break against a still-served major. It also derives the bump the diff
   requires and fails when the declared version does not match it. The second half is the one that
   matters: a negotiation is worth exactly the honesty of the number it announces, and the
   dangerous failure is silent, a client of major 1 accepting a server that broke it.

8. **The documentation stays one series per kind, with an optional scope in the name.** One
   numbered ADR series, one dated specification series, one dated handoff series, one backlog. The
   block that crosses the API and the web application is the block this repository exists for;
   splitting the series would tax exactly that block. The scope goes in the name, in the spelling
   conventional commits already use here.

9. **The web application ships as a static bundle and authenticates with a token.** No server side
   rendering and no Node runtime in the published image. An extension is necessarily cross origin
   and token based, so a rendering server with an `httpOnly` cookie would fork the authentication
   into two mechanisms and empty `clients/packages/auth` of its purpose.

10. **The hooks stop mutating the working tree.** `pre-commit` keeps the long dash refusal and
    nothing else. The contract's synchronisation becomes a gate verification that names the command
    to run, instead of a rewrite mid-commit that asks for the commit to be repeated. `pre-push`
    calls the Dagger gate, for the one reason that carries the whole decision: one command
    everywhere. It already calls `gate` today (`.githooks/pre-push`, last line), so nothing about
    its coverage changes; what changes is that the coverage is now the same object continuous
    integration runs.

11. **The coverage bound applies to logic, not to rendering.** Inside `clients/`, the bound covers
    the code the architecture separates from the view; the view is outside it, as `api-application`
    is outside it today for the same reason. A boundary rule (`dependency-cruiser` or
    `eslint-plugin-boundaries`) enforces the separation, because an isolation nothing checks holds
    only while someone remembers it.

## Consequences

- **Continuous integration loses its caches, and that is accepted for now.** Dagger keeps its state
  in a Docker volume destroyed with an ephemeral runner, so `gradle/actions/setup-gradle` and the
  `type=gha` buildx cache both go. Every pull request pays a full build. The exits, none free, are
  Dagger Cloud Engines, a runner with persistent storage, or backing up the engine volume; none is
  taken today and the decision is revisited when the wait costs more than the exits.
- **`pre-push` gets slower.** It goes through a container instead of the warm Gradle daemon of the
  session. The inner loop is untouched: the editor and `./gradlew :api-usecases:test --tests ...`
  stay native. The last gesture before pushing is where correctness beats speed.
- **`contract/` has no consumer until the first client lot**, and `contract/frozen/` stays empty
  during the alpha, where breaking is the stated policy. Both are surfaces whose real consumer
  arrives later, which the specification states block by block as
  `docs/adr/0018-a-block-is-a-pull-request.md` requires.
- **The pipeline needs git history.** Comparing the contract against its state on `main` requires
  the history in the container, so `fetch-depth: 0` in the workflow and a git aware input in the
  Dagger function.
- **The inline annotations of the official `oasdiff` action are lost.** Running the tool inside
  Dagger is what decision 5's rule dictates, and the annotations in the "Files changed" tab come
  with the action, not the binary. Emitting `::error` commands from the tool's output recovers them
  at a cost nobody has paid yet.
- **`info.version` stops describing the application.** It carries the contract version, which is
  independent of the image tag since decision 6. Nothing may fill it from
  `quarkus.application.version`.
- **Cross origin becomes structural rather than a development convenience.** The web application is
  now a third party origin like the extension, so `api.cors.origins` is part of every self hosted
  deployment's configuration, not a line pointing at a development server. The backlog's
  extension origin item gains a sibling.
- **The extension is the only client that forces backwards compatibility on the API**, because a
  store review and a user's own update schedule guarantee version skew. That constraint arrives
  with the extension, and it is better discovered before the beta than after.
- **A change in the web application no longer rebuilds the API image**, which is what decision 6
  buys. The price is a compatibility statement that has to be published and kept true, which is
  what decision 7 mechanises.
- **Two repository-wide checks leave Gradle.** `checkNoLongDashes` and `checkEvidenceGuard` cover
  the repository, not the API, and Gradle hosted them only because the two roots coincided. They
  become pipeline functions. This is not a rename to escape a constraint: the checks keep their
  scope, and the scope is what names their new home.
- **Continuous integration's static analysis widens, and closes a live gap.** `check` depends on
  `detektMain` and `detektTest`, which carry the type resolution rules; continuous integration runs
  `detekt`, `checkNoLongDashes`, `checkEvidenceGuard`, `test koverVerify` and
  `:api-application:quarkusBuild`, never `check`. So the type resolution rules run on no pull
  request today. A single call to the gate runs them, which widens what a pull request must satisfy
  and may surface findings the local gate has been catching alone.
- **The pipeline's SDK is an architectural choice and gets its own record.** The operator settled it
  in Discuss: TypeScript, which adds no language since `clients/` brings it anyway, where Java would
  put the only Java in a repository written in Kotlin. This ADR does not carry the decision: it is
  recorded as its own ADR, delivered with block 2, which is where the module it governs arrives.
