# Backend — Booking System API

REST API for the booking system (flight/hotel). Spring Boot 3, Java 17.

## Tech

- **Spring Boot** (Web, JPA, Validation, Cache, Actuator)
- **PostgreSQL** + Flyway migrations
- **Redis** (cache)

## Requirements

- Java 17
- PostgreSQL (port 5433)
- Redis (port 6379)

Run from **project root** first: `docker compose up -d`.

## Run

```bash
./mvnw spring-boot:run
```

**Base URL:** `http://localhost:8080`  
**Health:** `http://localhost:8080/actuator/health`

## Main endpoints

| Feature   | Path (example)                    |
|-----------|-----------------------------------|
| Inventory | `GET /api/inventory`, `GET /api/inventory/availability` |
| Booking   | `POST /api/bookings`, `GET /api/bookings/{id}` |
| Pay       | `POST /api/bookings/{id}/pay`     |

Database is initialized via Flyway; sample data (seeder) is available in dev.
