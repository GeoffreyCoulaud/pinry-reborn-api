# AGENTS.md

Pinry Reborn's API server: business logic of a self-hosted pin board (users, pins, boards, tags, images, exports),
exposed as an HTTP API. Kotlin + Quarkus, Clean Architecture, SQLite store, filesystem image storage, task worker for
long operations.

Process, engineering norms and writing conventions live in separate documents; read the one your task needs:

- `agents/workflow.md` : phases, tiers, the two reviews (mandates under `agents/reviews/`), backlog rules.
- `agents/engineering.md` : TDD, coverage, gate perimeter, Kotlin and backend norms.
- `agents/writing.md` : documentation regimes, language and style rules.
- `docs/handoffs/` : the newest file is the entry point (current state, pitfalls, next step).
- `docs/backlog.md` : open items only.

## Where things live

The repository holds several projects (`docs/adr/0024-three-projects-share-one-repository.md`).

| Path        | What                                                                                     |
|-------------|--------------------------------------------------------------------------------------------|
| `api/`      | The Gradle build: the twelve modules, `Dockerfile`, `config/`, `.idea/`.                     |
| `contract/` | The API's interface artefact, produced by `api/` and consumed by clients.                    |
| Root        | `docs/`, `agents/`, `scripts/`, `security/`, `.claude/`, `.github/`, `.githooks/`.           |

- **`contract/openapi.json` is generated and committed**, never edited by hand (`agents/writing.md`).
- **`contract/frozen/` holds one document per contract major still served**: a document enters when a
  major becomes still served and leaves when it stops being served. Empty during the alpha, where
  breaking is the stated policy of the README.

## Where the code lives

Twelve Gradle modules (`api/settings.gradle.kts`). Layering enforced by the build graph and
`ArchitectureKonsistTest`.

| Module                     | Role                                                                      |
|----------------------------|---------------------------------------------------------------------------|
| `api-domain`               | Pure: entities, enums, ports. No project dependency.                      |
| `api-usecases`             | Business logic, exceptions, search, exports, task contracts.              |
| `api-persistence-sqlite`   | Ebean/SQLite: models, mappers, repositories, migrations (`dbmigration/`). |
| `api-presentation-quarkus` | Jakarta REST: controllers, DTOs, mappers, security, OpenAPI.              |
| `api-storage-filesystem`   | Image store, rendition cache, export archives.                            |
| `api-imaging-vips`         | libvips adapter (vips-ffm).                                               |
| `api-fetch-http`           | Remote image fetch behind an address policy.                              |
| `api-system`               | Clock, bcrypt, token generation.                                          |
| `api-worker-quarkus`       | Task worker: dispatcher, handlers, export retention.                      |
| `api-utilities`            | Shared helpers, `BaseTest` fixture (testFixtures).                        |
| `api-application`          | Composition root + end-to-end integration tests.                          |
| `detekt-rules`             | Project detekt rules; outside the layering.                               |

## Setup (once per clone)

- `git config core.hooksPath .githooks` (enables pre-commit and pre-push hooks).
- Native libvips: `brew install vips` (macOS) or `libvips42t64` (Ubuntu 24.04), otherwise
  `api-imaging-vips` and image-touching integration tests cannot load the library.
- `python3` on the PATH (`.claude/hooks/evidence-guard.py` runs on every Bash command; without python3 it enforces
  nothing, silently).

## Commands

**Every Gradle command runs from `api/`**: the wrapper is `api/gradlew` and the settings file it needs
sits beside it, so `./api/gradlew` from the repository root finds no build at all. Prefix with
`cd api &&` from anywhere else.

- Runner: `./gradlew` (committed wrapper; JDK 25 toolchain auto-provisioned).
- **Gate (the single local knob)**: `./gradlew gate`
- One test: `./gradlew :api-usecases:test --tests "UserCreatorTest"`
- New migration (after changing an entity model): `./gradlew :api-persistence-sqlite:generateDbMigration`.
- Destructive migration (drop): re-run the generator with the property **in the generator's JVM**, not on the Gradle
  CLI:
  `JAVA_TOOL_OPTIONS="-Dddl.migration.pendingDropsFor=<version>" ./gradlew :api-persistence-sqlite:generateDbMigration`.
  Commit both pairs together. Precedent: `1.13` and `1.14__dropsFor_1.13`.
- No auto-fix task: detekt has no formatting rules, ktlint is IDE-only. Fix findings by hand.

## CI

CI (`validate.yml`) is not a caller of `gate`: it enumerates the gate's parts. A check added to
`gate` alone runs on no pull request. CI also builds the container image and checks the
`contract/openapi.json` sync. The gate covers neither: the `pre-commit` hook regenerates it,
`ImportDataDirectoryImageTest` and `ExportDataDirectoryImageTest` read the Dockerfile's ownership lines
from inside the gate, and the image build itself runs only in CI.

## Gotchas

- **A local merge to `main` bypasses CI** (`enforce_admins` is false). Always push and open a PR; merge is rebase-only
  (`gh pr merge --rebase`).
- **Never edit an applied migration**: the checksum changes and Ebean refuses the history. A correction is a new
  migration.
- **Unique constraint on SQLite**: `@Index(definition = "create unique index ...")`, never
  `unique = true` (Ebean emits an unsupported `ALTER TABLE` that becomes a silent no-op comment).
  `DbMigrationModelCoverageTest` fails on the no-op marker.
- **Partial or expression index**: `definition` alone, no `columnNames`, no `unique = true`.
- **The `pre-commit` hook rewrites `contract/openapi.json`**, stages it, and exits non-zero when it changed: re-run
  the commit. It also rejects em/en-dashes in staged text.
- **A changed detekt rule is not picked up by a live Gradle daemon** (cached classpath: false green). Run
  `./gradlew --stop` before trusting a local gate after a rule change.
- **detekt baselines are per module** (`api/config/detekt/baseline-<module>.xml`): the `detektBaseline`
  task rewrites rather than merges.
- **`checkNoLongDashes` and `checkEvidenceGuard` cover the repository, not the API**, so both name
  `rootDir.parentFile` (`api/build.gradle.kts`). Both halves matter: point only the exec at the
  repository and the read side silently finds no file and passes over nothing. They move to the
  pipeline in block 2 of `docs/specs/2026-09-08-monorepo.md`.
