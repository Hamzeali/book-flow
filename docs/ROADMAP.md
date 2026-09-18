# BookFlow — Hotel Room Booking Management System

BookFlow is a hotel room booking backend built with Spring Boot, created as a portfolio/learning
project. This document is a **self-paced roadmap**, not a spec to implement all at once — work
through the milestones in order, check off tasks as you complete them, and don't rush past a
milestone until you actually understand the concepts it introduces.

## Tech stack

| Area | Choice | Why |
|---|---|---|
| Language / runtime | Java 21 | Already set up in the scaffold |
| Framework | Spring Boot | Core of the project |
| Web layer | Spring Web (REST, no UI) | Practice building a clean JSON API |
| Persistence | Spring Data JPA | Standard ORM approach in Spring apps |
| Database (early) | H2 (in-memory) | Fast local iteration, zero setup |
| Database (later) | PostgreSQL + Docker | Realistic production-like setup |
| Migrations | Flyway | Learn versioned schema management |
| Validation | Bean Validation (`spring-boot-starter-validation`) | Request validation the idiomatic way |
| Security | Spring Security + JWT | Auth/roles, a common portfolio expectation |
| Testing | JUnit 5, Mockito, `@DataJpaTest`, `MockMvc` | Unit + integration testing practice |
| Docs | springdoc-openapi (Swagger UI) | Explorable API docs for free |
| Boilerplate reduction | Lombok (optional) | Less getter/setter noise |
| Deployment | Docker + a free host (Render/Railway/Fly.io) | End-to-end delivery, not just "runs on my machine" |

## Domain model

- **User** — `id`, `name`, `email`, `password`, `role` (`ADMIN` / `CUSTOMER`)
- **Hotel** — `id`, `name`, `address`, `city`
- **Room** — `id`, `hotel`, `roomNumber`, `type`, `pricePerNight`, `capacity`
- **Booking** — `id`, `room`, `user`, `checkInDate`, `checkOutDate`, `status` (`PENDING` / `CONFIRMED` / `CANCELLED`), `totalPrice`

Relationships: one `Hotel` has many `Room`s; one `Room` has many `Booking`s; one `User` has many `Booking`s.

```java
// Illustrative only — not the full entity
@ManyToOne
@JoinColumn(name = "hotel_id")
private Hotel hotel;
```

## Milestones

### M0 — Project setup & hygiene
Get the scaffold into good shape before writing domain code.

- [ ] Fill in `pom.xml` metadata (`name`, `description`, `url`, `license`)
- [ ] Remove `spring-boot-starter-thymeleaf` (REST-only API)
- [ ] Add dependencies: `h2`, `spring-boot-starter-validation`, `lombok` (optional)
- [ ] Create base packages: `entity`, `repository`, `service`, `controller`, `dto`, `exception`, `config`
- [ ] `git init`, initial commit, create the GitHub repo, push
- [ ] Write a starter `README.md` (what the project is, how to run it)

**Done when:** the app boots cleanly (`./mvnw spring-boot:run`) with an empty but organized package structure, committed to GitHub.

### M1 — Domain & persistence
Learn JPA entities, relationships, and repositories.

- [ ] Create `User`, `Hotel`, `Room`, `Booking` entities with correct JPA relationship annotations
- [ ] Create Spring Data repositories (`UserRepository`, `HotelRepository`, `RoomRepository`, `BookingRepository`)
- [ ] Enable and explore the H2 console
- [ ] Seed sample data (`data.sql` or a `CommandLineRunner`)
- [ ] Write `@DataJpaTest` tests proving repositories save/query correctly

**Done when:** you can start the app, open the H2 console, and see seeded hotels/rooms/users with correct relationships.

### M2 — Core REST API (Hotels & Rooms)
Build your first real CRUD API.

- [ ] Create request/response DTOs (don't expose entities directly)
- [ ] Add a service layer between controllers and repositories
- [ ] Implement CRUD controllers for `Hotel` and `Room`
- [ ] Add Bean Validation annotations on request DTOs (`@NotBlank`, `@Positive`, etc.)
- [ ] Add centralized error handling with `@RestControllerAdvice`
- [ ] Manually test every endpoint with curl or Postman

**Done when:** you can create/read/update/delete hotels and rooms via HTTP, with proper validation errors returned as clean JSON (not stack traces).

### M3 — Booking business logic
The interesting part — actual business rules.

- [ ] `POST /bookings` — create a booking for a room + date range
- [ ] Reject overlapping bookings for the same room (the core business rule)
- [ ] Implement booking status transitions (`PENDING` → `CONFIRMED` → `CANCELLED`)
- [ ] Calculate `totalPrice` from `pricePerNight` × nights
- [ ] `PATCH /bookings/{id}/cancel`
- [ ] List/filter bookings by user or by room

**Done when:** you cannot create two overlapping bookings for the same room, and cancelling/confirming works correctly.

### M4 — Testing
Prove the app works without manual clicking.

- [ ] Unit test services with Mockito (mock repositories)
- [ ] Cover booking edge cases: exact overlap, partial overlap, back-to-back dates, invalid date ranges
- [ ] `@DataJpaTest` for repository-level queries
- [ ] `@SpringBootTest` + `MockMvc` integration tests for controllers
- [ ] Check coverage on the booking overlap logic specifically — it's the highest-risk code in the app

**Done when:** `./mvnw test` passes and confidently covers the booking overlap/cancellation logic.

### M5 — Security (Auth & Roles)
Add authentication and authorization.

- [ ] Add Spring Security
- [ ] `POST /auth/register` and `POST /auth/login`
- [ ] Hash passwords with BCrypt
- [ ] Issue and validate JWTs
- [ ] Restrict hotel/room management to `ADMIN`
- [ ] Restrict booking actions to the authenticated `CUSTOMER` who owns them

**Done when:** unauthenticated requests are rejected, and a customer cannot manage another customer's bookings or hotels.

### M6 — PostgreSQL & migrations
Move off H2 toward a production-like setup.

- [ ] Add a `docker-compose.yml` for PostgreSQL
- [ ] Add Spring profiles (`dev` uses H2, `prod`/`docker` uses PostgreSQL)
- [ ] Introduce Flyway, converting seed data into versioned migration scripts
- [ ] Verify the app runs against PostgreSQL end-to-end

**Done when:** `docker compose up` gives you a Postgres instance the app connects to via Flyway-managed schema.

### M7 — API polish
Make the API feel production-grade.

- [ ] Add pagination and sorting (`Pageable`) to list endpoints
- [ ] Add search/filtering (e.g. available rooms by city + date range)
- [ ] Add springdoc-openapi for interactive Swagger UI docs
- [ ] Add structured logging
- [ ] Standardize API response/error shape across all endpoints

**Done when:** you can browse and try every endpoint from Swagger UI without external docs.

### M8 — Stretch goals (pick any that interest you)

- [ ] Mock email notification on booking confirmation
- [ ] Cache available-rooms queries
- [ ] Rate limiting on public endpoints
- [ ] Review/rating system for hotels
- [ ] Admin stats endpoint (occupancy rate, revenue, etc.)
- [ ] Scheduled job to auto-expire stale `PENDING` bookings

### M9 — Deployment & portfolio polish
Ship it and make it presentable.

- [ ] Dockerize the application
- [ ] Deploy to a free host (Render, Railway, or Fly.io)
- [ ] Write a polished `README.md`: architecture overview, setup instructions, API examples, screenshots or a short demo GIF
- [ ] Share a Postman collection or the Swagger UI link
- [ ] Tag a `v1.0` release on GitHub

**Done when:** someone can open your GitHub repo, read the README, and understand what BookFlow does and how to run it — without asking you anything.

## Suggested Git workflow

- Commit early and often with clear, conventional-ish messages (`feat:`, `fix:`, `test:`, `docs:`)
- Use one feature branch per milestone (or per task within a milestone)
- Open a pull request into `main` for each milestone, even solo — it's good practice for writing PR descriptions and reviewing your own diffs
- Track milestone checklists with GitHub Issues or a Projects board

## How to use this roadmap

Work through milestones **in order** — each one builds on the last. It's fine to spend extra time
on a milestone to fully understand a concept (especially M3's overlap logic and M5's security) —
depth beats speed here. Come back for a code review or design discussion at any checkpoint.
