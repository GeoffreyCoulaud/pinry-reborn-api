# Pinry Reborn

The API server lives in `api/`. It is in charge of all the business logic, to be called by clients.  
The OpenAPI contract it generates lives in `contract/`, which is what a client is written against
(`docs/adr/0024-three-projects-share-one-repository.md`).

> **Alpha. Do not deploy this yet.** Breaking changes and data loss are expected between versions, and the
> database migration history will be flattened before beta.

## Running

To start the API locally in dev mode

```sh
cd api && ./gradlew quarkusDev
```

## Git hooks

This repo ships its git hooks in `.githooks/`. Enable them once per clone:

```sh
git config core.hooksPath .githooks
```

- `pre-commit` refuses an em dash or an en dash in what you staged.
- `pre-push` runs `dagger call gate`, the same command CI runs: the API's build, the prose rules, the
  contract's synchronisation and the breaking-change guard. It needs Docker and the Dagger CLI.

## Architecture

The API follows the clean architecture principle, with each part in its own submodule.

