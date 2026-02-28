# Backend — Booking System API

Spring Boot REST API for the Scalable Booking System (flights/hotels).

## Run

From this folder (`backend/`):

```bash
./mvnw spring-boot:run
```

Or from project root:

```bash
cd backend && ./mvnw spring-boot:run
```

Before running: start PostgreSQL and Redis with `docker compose up -d` from the **project root** (where `docker-compose.yml` is).

API base: `http://localhost:8080`  
Health: `http://localhost:8080/actuator/health`
