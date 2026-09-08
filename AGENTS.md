# AGENTS.md

Pinry Reborn, a self-hosted pin board: users, pins, boards, tags, images and exports. The repository holds three
projects (`docs/adr/0024-three-projects-share-one-repository.md`): the API server that owns the business logic, and the
web application and browser extension that will consume it.

Process, engineering norms and writing conventions live in separate documents; read the one your task needs:

- `agents/workflow.md` : phases, tiers, the two reviews (mandates under `agents/reviews/`), backlog rules.
- `agents/engineering.md` : TDD, coverage, gate perimeter, Kotlin and backend norms.
- `agents/writing.md` : documentation regimes, language and style rules.
- `docs/handoffs/` : the newest file is the entry point (current state, pitfalls, next step).
- `docs/backlog.md` : open items only.

**The process is shared and the technical norms are not.** Each ecosystem root carries its own `AGENTS.md` for its
norms, its commands and its gate; this file carries what holds for the repository.

## Where things live

| Path        | What                                                                                                      |
|-------------|-----------------------------------------------------------------------------------------------------------|
| `api/`      | The Gradle build: the twelve modules, `Dockerfile`, `config/`, `.idea/`. Read `api/AGENTS.md` before touching it. |
| `contract/` | The API's interface artefact, produced by `api/` and consumed by the clients.                              |
| `clients/`  | Not built yet: the web application, the browser extension and their shared packages, with their own `AGENTS.md` when they arrive. |
| `.dagger/`  | The pipeline, in TypeScript (`docs/adr/0025-the-pipeline-is-written-in-typescript.md`). Belongs to no ecosystem: it calls both. |
| Root        | `docs/`, `agents/`, `security/`, `.claude/`, `.github/`, `.githooks/`, `dagger.json`.                      |

- **`contract/openapi.json` is generated and committed**, never edited by hand (`agents/writing.md`).
- **`contract/frozen/` holds one document per contract major still served**: a document enters when a
  major becomes still served and leaves when it stops being served. Empty during the alpha, where
  breaking is the stated policy of the README.

## Setup (once per clone)

- `git config core.hooksPath .githooks` (enables pre-commit and pre-push hooks).
- **Docker and the Dagger CLI**, which the gate and the `pre-push` hook both go through. `dagger.json` pins the
  engine version; install the CLI at that version.
- `python3` on the PATH (`.claude/hooks/evidence-guard.py` runs on every Bash command; without python3 it enforces
  nothing, silently).
- Each ecosystem root has its own setup steps on top of these; `api/AGENTS.md` carries the API's.

## The gate

**One command, from anywhere in the repository: `dagger call gate`.** It is what `pre-push` runs and what CI
runs, in the same container, and it holds three things:

| Function                | What it runs                                                                        |
|-------------------------|--------------------------------------------------------------------------------------|
| `dagger call api-gate`  | The API's Gradle gate (`api/AGENTS.md`), with the JDK, libvips and python3 pinned.    |
| `dagger call prose`     | No long dash in a tracked text file, and the evidence guard's own tests.              |
| `dagger call contract`  | Produces `contract/openapi.json`. `gate` refuses a committed document that differs.   |

A check whose scope is the repository goes to `.dagger/`; a check whose scope is one ecosystem goes to that
ecosystem's own gate.

## CI

CI (`validate.yml`) **calls** the gate: one job, one `dagger call gate`, the command a workstation types. A check
added to the pipeline is on the next pull request with nothing to add here. What CI still holds alone is the
container image, which it builds on every run, smoke-tests, and publishes on the release path.

## Gotchas

- **A local merge to `main` bypasses CI** (`enforce_admins` is false). Always push and open a PR; merge is rebase-only
  (`gh pr merge --rebase`).
- **Nothing regenerates `contract/openapi.json` for you.** The gate refuses a stale document and names the command
  that refreshes it; the `pre-commit` hook rejects em/en-dashes in staged additions and does nothing else.
