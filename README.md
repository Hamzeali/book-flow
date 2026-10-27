# BookFlow

BookFlow is a **hotel room booking management system** built with Spring Boot, developed as a
learning project to practice backend development skills (REST APIs, JPA, security, testing).

> **Status:** in development — hotel, room, and booking APIs, automated tests, JWT authentication, and PostgreSQL with Flyway migrations are implemented; API polish (pagination, Swagger UI) is next.

## Tech stack

- Java 21
- Spring Boot (Spring Web, Spring Data JPA, Bean Validation)
- Spring Security with JWT (OAuth2 resource server)
- PostgreSQL (Docker) with Flyway migrations; H2 in-memory for development and tests
- JUnit 5 / Mockito (testing)
- Maven

## Getting started

### Prerequisites

- JDK 21+
- Docker (only for the PostgreSQL profile)

### Run the application

The app has two profiles:

| Profile | Database | Data |
|---|---|---|
| `dev` (default) | In-memory H2 | Demo hotel, rooms, and users are seeded on startup |
| `postgres` | PostgreSQL in Docker | Starts empty; an admin is created from `ADMIN_EMAIL` / `ADMIN_PASSWORD` |

In both profiles, the schema is created by Flyway migrations (`src/main/resources/db/migration`).

#### With H2 (no setup)

```bash
./mvnw spring-boot:run
```

#### With PostgreSQL

```bash
cp .env.example .env        # then edit the values
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

`.env` is git-ignored and read by both Docker Compose and Spring Boot. Real environment variables take precedence over it.

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
├── security/     # Authenticated-user helpers
└── service/      # Business logic
```

## Roadmap

Development follows a milestone-based roadmap:

- [x] Project setup
- [x] Domain model & persistence (JPA, H2)
- [x] REST API for hotels and rooms (DTOs, validation, error handling)
- [x] Booking logic (overlap detection, pricing, status transitions, filtering)
- [x] Automated tests (Mockito, MockMvc)
- [x] Authentication & roles (Spring Security, JWT)
- [x] PostgreSQL & Flyway migrations
- [ ] API polish (pagination, Swagger UI)
- [ ] Deployment
