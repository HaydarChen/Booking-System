# Scalable Booking System (Travel Booking)

A simplified travel booking system (flights/hotels) built to demonstrate **concurrency handling**, **horizontal scaling readiness**, and **clean architecture**.

## Learning-First Approach

This project is structured so you **understand before you code**:

- **[LEARNING_GUIDE.md](./LEARNING_GUIDE.md)** — Step-by-step explanation of:
  - Problem domain (inventory, consistency, high traffic)
  - Layered architecture (Controller, Service, Repository, DTO)
  - Tech stack (Java, Spring Boot, PostgreSQL, Redis, Docker)
  - Concurrency (optimistic locking, distributed lock)
  - Horizontal scaling (stateless API, session in Redis, idempotency)
  - Exception handling and tech debt awareness

- **[PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)** — Checklist of what to build (packages, entities, services, controllers) and in what order. No code inside — you implement it.

## Features (What You Will Implement)

- Search flights / hotels and check availability
- Book flight or hotel with **double-booking prevention** (optimistic locking and/or distributed lock)
- **Payment simulation** (no real payment)
- **Stateless API** with session stored in Redis
- **Idempotency key** for booking requests
- Clear **Controller → Service → Repository** structure and DTO mapping
- Centralized **exception handling** and error DTOs

## Tech Stack

**Backend** — in the [`backend/`](./backend) folder  
- **Java** + **Spring Boot**
- **PostgreSQL** (primary data)
- **Redis** (caching, session, optional distributed lock)
- **Docker** (PostgreSQL + Redis; `docker-compose.yml` at project root)

**Frontend** — in the [`frontend/`](./frontend) folder  
- **Next.js** (App Router), **TypeScript**, **Tailwind CSS**. See [frontend/README.md](./frontend/README.md) for setup and run.

## Project layout (root)

```
booking-system/
├── backend/          # Spring Boot API
├── frontend/         # Next.js app
├── docker-compose.yml
├── README.md
└── ...
```

## How to Run (After Implementation)

**Backend**
1. From project root: `docker compose up -d`
2. `cd backend && ./mvnw spring-boot:run`
3. API is at `http://localhost:8080`

**Frontend**
1. `cd frontend && cp .env.example .env.local` (set `NEXT_PUBLIC_API_URL=http://localhost:8080` if needed)
2. `npm install && npm run dev`
3. Open [http://localhost:3000](http://localhost:3000)

## Known Limitations (Template — Fill When You Implement)

- *(Example: No real payment; only simulated success/failure.)*
- *(Example: Cache invalidation is TTL-based, not event-based.)*

## Future Improvements (Template — Fill When You Implement)

- *(Example: Add Kafka for booking confirmation events and async processing.)*
- *(Example: Integrate a real payment provider and webhooks.)*

---

Start with **LEARNING_GUIDE.md**, then use **PROJECT_STRUCTURE.md** as your implementation map.
