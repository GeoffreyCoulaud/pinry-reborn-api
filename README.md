# Pinry Reborn

The API server lives in `api/`. It is in charge of all the business logic, to be called by clients.  
The OpenAPI contract it generates lives in `contract/`, which is what a client is written against
(`docs/adr/0024-three-projects-share-one-repository.md`).  
The clients live in `clients/`, a pnpm workspace: the web application in `clients/apps/webapp`, and the
browser extension when it arrives.

> **Alpha. Do not deploy this yet.** Breaking changes and data loss are expected between versions, and the
> database migration history will be flattened before beta.

## Running

To start the API locally in dev mode

```sh
cd api && ./gradlew quarkusDev
```

To start the web application against it, with Node 24 and any recent pnpm installed
(`clients/package.json` names the exact pnpm and an installed one switches itself to it):

```sh
cd clients && pnpm install
pnpm --filter @pinry-reborn/webapp run dev
```

The development server proxies `/api` to `http://localhost:8080`, so the application and the API share
one origin, which is what the session cookie needs (`docs/adr/0026-one-session-two-transports.md`).
`clients/AGENTS.md` carries the rest of the commands.

## Git hooks

This repo ships its git hooks in `.githooks/`. Enable them once per clone:

```sh
git config core.hooksPath .githooks
```

- `pre-commit` refuses an em dash or an en dash in what you staged.
- `pre-push` runs `dagger call gate`, the same command CI runs: the API's build, the clients' build and
  tests, the prose rules, the contract's synchronisation and the breaking-change guard. It needs Docker
  and the Dagger CLI.

## Architecture

The API follows the clean architecture principle, with each part in its own submodule.

