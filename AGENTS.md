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
| Root        | `docs/`, `agents/`, `scripts/`, `security/`, `.claude/`, `.github/`, `.githooks/`.                         |

- **`contract/openapi.json` is generated and committed**, never edited by hand (`agents/writing.md`).
- **`contract/frozen/` holds one document per contract major still served**: a document enters when a
  major becomes still served and leaves when it stops being served. Empty during the alpha, where
  breaking is the stated policy of the README.

## Setup (once per clone)

- `git config core.hooksPath .githooks` (enables pre-commit and pre-push hooks).
- `python3` on the PATH (`.claude/hooks/evidence-guard.py` runs on every Bash command; without python3 it enforces
  nothing, silently).
- Each ecosystem root has its own setup steps on top of these; `api/AGENTS.md` carries the API's.

## CI

CI (`validate.yml`) is not a caller of `gate`: it enumerates the gate's parts. A check added to
`gate` alone runs on no pull request. CI also builds the container image and checks the
`contract/openapi.json` sync. The gate covers neither: the `pre-commit` hook regenerates it,
`ImportDataDirectoryImageTest` and `ExportDataDirectoryImageTest` read the Dockerfile's ownership lines
from inside the gate, and the image build itself runs only in CI.

## Gotchas

- **A local merge to `main` bypasses CI** (`enforce_admins` is false). Always push and open a PR; merge is rebase-only
  (`gh pr merge --rebase`).
- **The `pre-commit` hook rewrites `contract/openapi.json`**, stages it, and exits non-zero when it changed: re-run
  the commit. It also rejects em/en-dashes in staged text, which the gate holds over the whole tree.
