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

This is a Kotlin API server following **Clean Architecture** with strict layer boundaries.

### Modules

```
api-domain/              # Domain entities & repository interfaces
api-usecases/            # Business logic use cases
api-persistence-sqlite/  # Ebean ORM implementations
api-presentation-quarkus/# REST controllers & DTOs
api-application/         # Entry point & integration tests
api-utilities/           # Shared utilities & test fixtures
```

### Dependency Rules (STRICT)

| Module | May Depend On |
|--------|---------------|
| `api-domain` | Nothing (pure). May use `api-utilities` if absolutely necessary. |
| `api-usecases` | `api-domain` only |
| `api-persistence-sqlite` | `api-domain`, `api-utilities` |
| `api-presentation-quarkus` | `api-usecases`, `api-domain` |
| `api-application` | All modules (composition root) |

**Never poke holes through layers.** Presentation must not call persistence directly. Use cases must not depend on persistence implementations.

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

## Development Workflow (TDD)

Follow the user's instructions, but always suggest Test-Driven Development.

### Testing Order

Write tests in this order, each failing before moving to implementation:

1. **Integration tests** (`api-application`) - REST Assured end-to-end tests
2. **Use-case unit tests** (`api-usecases`) - MockK-based business logic tests
3. **Repository tests** (`api-persistence-sqlite`) - Ebean database tests

### Red-Green-Refactor Cycle

1. **Red**: Write a failing test
2. **Green**: Write minimal code to make it pass
3. **Refactor**: Clean up while keeping tests green

### Test Naming Convention

Test names use backticks with **"Given..., Then..."** format (no "when" in the name):

```kotlin
@Test
fun `Given a valid user, Then creation succeeds`() { ... }

@Test
fun `Given duplicate username, Then throws UserCreationError`() { ... }
```

### Test Body Structure

Tests follow **Given-When-Then** structure with explicit comments:

```kotlin
@Test
fun `Given valid credentials, Then authentication succeeds`() {
    // Given
    val username = "testuser"
    val password = "password123"

    // When
    val result = authenticator.authenticate(username, password)

    // Then
    assertNotNull(result)
}
```

### Test Maintainability

- **Create helper methods** for repeated setup (e.g., `createAndSaveUser()`)
- **Use test variables** with meaningful names, not inline literals
- **Leverage `createRandomString()`** from utilities for unique test data
- **Extend base test classes**: `IntegrationTest`, `BaseTest`, `RepositoryTest`

## Module Conventions

- Domain entities in `api-domain/entities/` have corresponding repository interfaces in `api-domain/repositories/`
- Persistence implementations in `api-persistence-sqlite/repositories/` use mappers in `mappers/` to convert between DB
  models and domain entities
- Use cases in `api-usecases/` throw domain-specific exceptions (e.g., `UserCreationError`, `PinCreationError`)
- REST controllers in `api-presentation-quarkus/controllers/` use DTOs in `dtos/` for input/output