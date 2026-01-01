# Pinry Reborn - API server

This directory contains the API server for the project.  
It is in charge of all the business logic, to be called by clients.

## Running

To start the API locally in dev mode

```sh
./gradlew quarkusDev
```

## Architecture

The API follows the clean architecture principle, with each part in its own submodule.
- `api-domain` contains all the domain models, and is independent
- `api-persistence-sqlite` implements persistence using sqlite + ebean
- `api-usecases` implements the business logic, on top of persistence
- `api-presentation-rest` implements a JSON REST API for the business logic, using Quarkus
- `api-application` wires everything together

```mermaid
graph TB
    APP[api-application<br/>Wires everything together]
    REST[api-presentation-rest<br/>REST API - Quarkus]
    UC[api-usecases<br/>Business Logic]
    PERSIST[api-persistence-sqlite<br/>Persistence - SQLite + Ebean]
    DOMAIN[api-domain<br/>Domain Models]
    
    APP --> REST
    APP --> UC
    APP --> PERSIST
    
    REST --> UC
    UC --> PERSIST
    
    PERSIST -.-> DOMAIN
    UC -.-> DOMAIN
    REST -.-> DOMAIN
```

