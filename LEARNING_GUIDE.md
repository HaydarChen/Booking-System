<!-- # Learning Guide — Scalable Booking System -->

This guide teaches you **why** each part of the project exists and **how** it fits into software engineering. Use it to understand before you code.

---

## Part 1: Understanding the Problem Domain

### Why "Travel" is a Good Interview Project

Travel booking systems combine three hard problems:

1. **Inventory** — Flights and hotels have limited seats/rooms. You must show what’s available and update it correctly.
2. **Consistency** — Two users must not book the same seat. Data must stay correct under concurrency.
3. **High traffic** — Many users search and book at once. The system must scale and stay fast.

Your project should show you understand these three.

---

## Part 2: Architecture — The Layers and Why They Exist

### 2.1 Layered Architecture (Controller → Service → Repository)

```
[Client] → Controller → Service → Repository → Database
                ↓           ↓           ↓
              DTOs      Business    Entities
                        logic
```

**Controller**
- **Role:** HTTP in/out. Parse request, validate input, call service, return response.
- **Why separate:** So HTTP details (JSON, status codes) don’t mix with business rules. Easier to test and change (e.g. add GraphQL later).

**Service**
- **Role:** Business logic. “Can this user book this flight?” “Apply discount.” “Reserve inventory.”
- **Why separate:** One place for rules. Reusable from controllers, jobs, or other services. No SQL here.

**Repository**
- **Role:** Data access. Save/load entities. Queries. No business rules.
- **Why separate:** You can swap database or add caching without touching business logic.

**DTO (Data Transfer Object)**
- **Role:** Objects used only for API request/response. Often different from database entities (e.g. hide internal IDs, combine fields).
- **Why:** Decouples API contract from database schema. Versioning and validation are clearer.

**Takeaway:** Each layer has one responsibility. That’s Single Responsibility and separation of concerns.

---

## Part 3: Tech Stack — What Each Piece Does

### Java + Spring Boot
- **Java:** Strong typing, good concurrency support (locks, atomic operations).
- **Spring Boot:** Dependency injection, web (REST), data (JPA), configuration. Lets you focus on business logic.

### PostgreSQL
- **Why:** ACID, good for “book this seat” and “deduct inventory” with transactions.
- **Use for:** Flights, hotels, bookings, users — anything that must be consistent.

### Redis
- **Why:** In-memory, very fast. Good for data that changes often and can be recreated.
- **Use for:**
  - **Caching:** e.g. “list flights for route X” to reduce DB load.
  - **Session store:** Session data so the app stays stateless (see Part 4).
  - **Distributed lock:** Coordinate “only one server can book this resource” (see Part 5).

### Docker
- **Why:** Same environment everywhere (Java app, PostgreSQL, Redis). “Runs on my machine” becomes “runs in a container.”
- **Use for:** One `docker-compose` to start app + DB + Redis for development and demos.

### Kafka (Optional)
- **Why:** Async messaging. One service publishes “BookingConfirmed”, others consume (emails, analytics).
- **Use for:** Decoupling booking from notifications/analytics; better scalability and reliability.

---

## Part 4: Horizontal Scaling — Stateless API and Session in Redis

### 4.1 What “Horizontal Scaling” Means

- **Vertical:** Bigger machine (more CPU/RAM).
- **Horizontal:** More machines (more instances of your API). A load balancer sends requests to any instance.

For horizontal scaling, **any instance must be able to serve any request**. So the API must be **stateless**.

### 4.2 Stateless API

- **Stateful (bad for scaling):** “User session” lives only in one server’s memory. If the next request hits another server, it doesn’t know the user.
- **Stateless (good):** Server does not keep user state in memory. State is either:
  - in the **client** (e.g. JWT in header), or
  - in a **shared store** (e.g. Redis).

So: **no session storage in app memory**. Store session in **Redis** and use a session id (or JWT) from the client so any instance can load that session from Redis.

**What to implement:**
- Session store in Redis (e.g. Spring Session with Redis).
- Login returns a session/token; client sends it every time.
- Any API instance can validate the token and load session from Redis.

### 4.3 Idempotency Key for Booking

**Problem:** User clicks “Book” twice (double-click or retry). Without care, you create two bookings.

**Idempotency:** Same request (same idempotency key) applied multiple times = same effect as applied once.

**How:**
- Client sends a unique **idempotency key** (e.g. UUID) in header or body for “create booking”.
- Server:
  - First time: process booking, store result under that key (e.g. in Redis with TTL).
  - Same key again: return the **same** stored response, do not create another booking.

**What to implement:**
- Header like `Idempotency-Key: <uuid>`.
- In the booking flow: check Redis for that key; if present return cached response; if not, run booking and cache response by key.

---

## Part 5: Concurrency — Preventing Double Booking

Two users (or one user twice) must not get the same seat/room. Two main approaches:

### 5.1 Optimistic Locking

**Idea:** Assume conflicts are rare. You read, compute, then write only if the version hasn’t changed.

- Add a **version** (or timestamp) column to the resource (e.g. `Flight` or `Seat`).
- Read: get current version.
- Update: `UPDATE ... SET ..., version = version + 1 WHERE id = ? AND version = ?`. If no row updated → someone else changed it → conflict.
- In app: catch conflict and retry or return “already booked”.

**When to use:** Good for moderate contention (e.g. many seats, many flights). Simple and works well with PostgreSQL.

**What to implement:**
- `@Version` on entity (JPA).
- In booking: load seat/flight, try to update with new version; if `OptimisticLockException` (or 0 rows updated), return “not available” or retry.

### 5.2 Distributed Lock (e.g. Redis)

**Idea:** Before updating inventory, “lock” the resource so only one server can do it.

- Use Redis: `SET key value NX EX seconds` (only set if not exists, with expiry).
- Key = e.g. `lock:booking:flightId:seatId`.
- Before booking: acquire lock. If you get it, do the booking and release. If not, wait or return “please retry”.

**When to use:** When you need strict “one at a time” per resource (e.g. last seat). Complements optimistic locking.

**What to implement:**
- For the “book this seat” path: try to acquire Redis lock for that seat; then do DB update (optionally with optimistic locking); then release lock. Use try/finally so you always release.

**Takeaway:** You can use **optimistic locking** as the main mechanism and **Redis lock** to reduce contention on the same seat (optional but good to show you know both).

---

## Part 6: Exception Handling

**Goal:** No stack traces to the client. Consistent error format and HTTP status.

**Approach:**
- **Controller:** Don’t catch every exception. Let them bubble to a global handler.
- **Global exception handler:** e.g. `@ControllerAdvice` + `@ExceptionHandler`:
  - `ResourceNotFoundException` → 404 + `{ "code": "...", "message": "..." }`.
  - `DuplicateBookingException` or conflict → 409.
  - Validation errors → 400.
  - Generic → 500 and log, don’t expose internals.

**DTO for errors:** One structure for all API errors (e.g. `code`, `message`, optional `details`). Same in success and error responses when useful.

---

## Part 7: What Your README Should Include (Tech Debt Awareness)

Show that you know the limits and next steps:

### Known Limitations
- Example: “No real payment; we only simulate success/failure.”
- Example: “Cache invalidation is time-based, not event-based.”
- Example: “Idempotency keys are stored in Redis with TTL; after expiry duplicate request could be accepted.”

### Future Improvements
- Example: “Add event-driven flow with Kafka for confirmation emails and analytics.”
- Example: “Use proper payment provider (Stripe) and webhooks.”
- Example: “Implement event-sourced inventory for full audit trail.”

This shows you think in terms of production and trade-offs, not just “it works.”

---

## Part 8: Suggested Order of Implementation

1. **Setup**
   - Spring Boot project (Java, Web, JPA, Redis).
   - Docker Compose: PostgreSQL + Redis.
   - Basic health checks (app, DB, Redis).

2. **Domain and persistence**
   - Entities: e.g. Flight, Hotel, Booking (with version for optimistic locking).
   - Repositories (JPA).
   - Simple REST: list flights, get flight by id.

3. **Business logic**
   - Service: search (flights/hotels), check availability, create booking.
   - DTOs for request/response.
   - Controllers that use services and return DTOs.

4. **Concurrency**
   - Add optimistic locking on the entity that represents “inventory” or “booking”.
   - Optionally: Redis lock around “book this seat” to show distributed locking.

5. **Resilience and scaling**
   - Session in Redis (stateless API).
   - Idempotency key for booking endpoint.
   - Caching (e.g. search results or availability in Redis).

6. **Quality**
   - Global exception handler and error DTO.
   - README: architecture, how to run, **known limitations**, **future improvements**.

---

## Part 9: How to Use This Guide

- **Before coding a layer:** Re-read the section for that layer and the tech stack. Decide what you’ll implement (e.g. “booking with optimistic locking + Redis lock”).
- **When stuck:** Check the “What to implement” bullets; they are your acceptance criteria.
- **For the README:** Use Part 7 as a checklist so you show tech debt awareness.

You don’t need to change this guide’s text to “modify code” — the guide is for understanding. Implement the ideas in your own code step by step.

Good luck with your internship applications.
