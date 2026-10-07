# BookFlow

BookFlow is a **hotel room booking management system** built with Spring Boot, developed as a
learning project to practice backend development skills (REST APIs, JPA, security, testing).

> **Status:** in development — hotel, room, and booking APIs are implemented; automated testing, authentication, and PostgreSQL are next.

## Tech stack

- Java 21
- Spring Boot (Spring Web, Spring Data JPA, Bean Validation)
- H2 (in-memory, for development) → PostgreSQL (later milestone)
- JUnit 5 / Mockito (testing)
- Maven

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

Development follows a milestone-based roadmap:

- [x] Project setup
- [x] Domain model & persistence (JPA, H2)
- [x] REST API for hotels and rooms (DTOs, validation, error handling)
- [x] Booking logic (overlap detection, pricing, status transitions, filtering)
- [ ] Automated tests (Mockito, MockMvc)
- [ ] Authentication & roles (Spring Security, JWT)
- [ ] PostgreSQL & Flyway migrations
- [ ] API polish (pagination, Swagger UI)
- [ ] Deployment
