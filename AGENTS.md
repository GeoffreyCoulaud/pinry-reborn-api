This file provides guidance to AI Agents when working with code in this repository.

## Build Commands

```bash
./gradlew quarkusDev                              # Start dev server with hot reload
./gradlew test                                    # Run all tests
./gradlew :api-usecases:test                      # Run tests for a specific module
./gradlew :api-usecases:test --tests "UserCreatorTest"  # Run a single test class
./gradlew build                                   # Build the application
./gradlew :api-persistence-sqlite:generateDbMigration   # Generate Ebean DB migrations
```

## Architecture

This is a Kotlin API server following **Clean Architecture** with these submodules:

```
api-domain/              # Domain entities & repository interfaces (no dependencies)
api-usecases/            # Business logic use cases (depends on domain)
api-persistence-sqlite/  # Ebean ORM implementations (depends on domain, utilities)
api-presentation-quarkus/# REST controllers & DTOs (depends on domain, usecases)
api-application/         # Entry point & integration tests (depends on all)
api-utilities/           # Shared utilities & test fixtures
```

**Dependency flow**: presentation → usecases → domain ← persistence

## Key Technologies

- **Quarkus 3** - REST framework with Jakarta REST, HTTP Basic Auth via Quarkus Security
- **Ebean 17** - ORM with Kotlin query beans and SQLite
- **Java 21** (Adoptium)
- **Testing**: JUnit 5, MockK, REST Assured

## Database Migrations

Migrations live in `api-persistence-sqlite/src/main/resources/dbmigration/`. To generate a new migration after changing
entity models:

```bash
./gradlew :api-persistence-sqlite:generateDbMigration
```

## Testing Patterns

- **Unit tests**: Use MockK for mocking, located in `api-usecases` and `api-presentation-quarkus`
- **Integration tests**: Extend `IntegrationTest` base class in `api-application`, which provides clean DB state per
  test
- **Repository tests**: Ebean-based tests in `api-persistence-sqlite`

## Module Conventions

- Domain entities in `api-domain/entities/` have corresponding repository interfaces in `api-domain/repositories/`
- Persistence implementations in `api-persistence-sqlite/repositories/` use mappers in `mappers/` to convert between DB
  models and domain entities
- Use cases in `api-usecases/` throw domain-specific exceptions (e.g., `UserCreationError`, `PinCreationError`)
- REST controllers in `api-presentation-quarkus/controllers/` use DTOs in `dtos/` for input/output