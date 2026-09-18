# BookFlow

BookFlow is a **hotel room booking management system** built with Spring Boot, developed as a
learning project to practice backend development skills (REST APIs, JPA, security, testing).

> **Status:** early development (M0 complete) — following the milestone plan in [docs/ROADMAP.md](docs/ROADMAP.md).

## Tech stack

- Java 21
- Spring Boot (Spring Web, Spring Data JPA, Bean Validation)
- H2 (in-memory, for development) → PostgreSQL (later milestone)
- JUnit 5 / Mockito (testing)
- Maven

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full milestone-by-milestone plan, including
upcoming work on booking logic, Spring Security/JWT auth, and PostgreSQL migration.

## Getting started

### Prerequisites

- JDK 21+
- No local database needed yet — the app uses an in-memory H2 database

### Run the application

```bash
./mvnw spring-boot:run
```

### Run the tests

```bash
./mvnw test
```

## Project structure

```
src/main/java/de/bookflow/
├── config/       # Spring configuration classes
├── controller/   # REST controllers
├── dto/          # Request/response data transfer objects
├── entity/       # JPA entities
├── exception/    # Custom exceptions and error handling
├── repository/   # Spring Data JPA repositories
└── service/      # Business logic
```

## Roadmap

Development follows a milestone-based roadmap, from domain modeling through booking business
logic, authentication, and deployment. See [docs/ROADMAP.md](docs/ROADMAP.md) for details.
