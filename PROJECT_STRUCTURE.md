# Project Structure — What You Will Create

This document lists the **folders and files** you will create for the Scalable Booking System. It does **not** contain implementation code — use it as a checklist and map while you code.

Refer to **LEARNING_GUIDE.md** for the *why* behind each layer and technology.

---

## 1. Root and Configuration

| Path | Purpose |
|------|--------|
| `backend/pom.xml` (or `build.gradle`) | Dependencies: Spring Boot Web, Data JPA, Redis, PostgreSQL driver, validation, etc. |
| `docker-compose.yml` (at project root) | Start PostgreSQL and Redis with one command. Optionally your app. |
| `README.md` (at project root) | How to run, architecture summary, **Known limitations**, **Future improvements** (see LEARNING_GUIDE Part 7). |
| `backend/src/main/resources/application.yml` | DB URL, Redis host, server port. Use profiles (e.g. `application-dev.yml`) if you want. |

---

## 2. Domain and Persistence (Repository Layer)

**Entities (JPA):**
- Flight (id, flightNumber, route, departureTime, capacity, **version** for optimistic locking, etc.)
- Hotel (id, name, location, etc.)
- RoomType or HotelRoom (id, hotelId, type, totalRooms, **version**, etc.)
- Booking (id, userId/session, flightId or roomId, status, createdAt, idempotencyKey if stored, etc.)

**Repositories (Spring Data JPA):**
- `FlightRepository` — e.g. `findByRouteAndDate`, `findById`
- `HotelRepository` — e.g. `findById`, `findByLocation`
- `RoomRepository` (or equivalent) — e.g. `findByHotelId`, `findById`
- `BookingRepository` — e.g. `findById`, `findByIdempotencyKey`, `existsByFlightIdAndSeatNumber` (or similar to prevent double book)

Use `@Version` on the entity that represents inventory (e.g. Flight or a Seat/Inventory entity) so optimistic locking works.

---

## 3. DTOs (Data Transfer Objects)

**Request DTOs:**
- Search: e.g. `FlightSearchRequest` (origin, destination, date)
- Booking: e.g. `CreateBookingRequest` (flightId, seatNumber or similar, **idempotency key** in header or body)

**Response DTOs:**
- `FlightDTO`, `HotelDTO` — what the API returns (no internal fields you don’t want to expose)
- `BookingResponse` — booking id, status, flight summary, etc.
- **Error response DTO** — e.g. `code`, `message`, `details` for consistent error format

Put these in `backend/.../dto` (or `api.dto`).

---

## 4. Service Layer

**Services:**
- `FlightService` — search flights, get by id, (optionally) check availability
- `HotelService` — search hotels, get by id, (optionally) check room availability
- `BookingService` — check availability, create booking (with idempotency and lock/optimistic handling), payment simulation
- Optional: `AvailabilityService` — central place for “can this seat/room be booked?”

**Responsibilities to implement here:**
- Idempotency: check Redis (or DB) for existing response by idempotency key; if found return it, else run booking and store result.
- Distributed lock: before updating inventory, acquire Redis lock for the resource (e.g. flight+seat), then update, then release.
- Optimistic locking: update entity with version; catch `OptimisticLockException` and return “already booked” or retry.
- Payment simulation: e.g. random success/failure or fixed rule — no real payment gateway.

---

## 5. Controller Layer

**Controllers:**
- `FlightController` — e.g. `GET /flights/search`, `GET /flights/{id}`
- `HotelController` — e.g. `GET /hotels/search`, `GET /hotels/{id}`
- `BookingController` — e.g. `POST /bookings` (with idempotency key in header), `GET /bookings/{id}`

**Also:**
- **Global exception handler** (`@ControllerAdvice`): map exceptions to HTTP status and your error DTO (404, 409, 400, 500).

Controllers should be thin: parse input, call one or more services, return DTOs.

---

## 6. Configuration and Cross-Cutting

| What | Where / How |
|------|-------------|
| Redis connection | Spring Data Redis in `application.yml` |
| Session in Redis | Spring Session with Redis (so session is not in app memory — stateless API) |
| Idempotency key | Read from header (e.g. `Idempotency-Key`) in controller or filter; pass to service; store result in Redis by key |
| Distributed lock | Redis `SET key NX EX` or a small helper/filter that uses Redis; use in `BookingService` before updating inventory |

---

## 7. Optional (Kafka)

If you add Kafka:
- `BookingConfirmed` event (or similar) published after a successful booking.
- Consumer (same or separate app): e.g. log “send email” or “update analytics” — no need for real email.

---

## 8. Suggested Package Layout (under `backend/src/main/java/`)

```
com.yourname.booking
├── config          # Redis, Session, Kafka (if any)
├── controller      # REST controllers
├── dto             # Request/response DTOs
├── entity          # JPA entities
├── exception       # Custom exceptions + @ControllerAdvice
├── repository      # JPA repositories
├── service         # Business logic
└── (optional) lock # Redis lock helper
```

---

## How to Use This

1. Create the structure (packages and empty or minimal files) first.
2. Implement in the order suggested in **LEARNING_GUIDE.md** Part 8: setup → domain/repos → services → concurrency → session/idempotency/cache → exception handling → README.
3. For each file, ask: “What is this layer allowed to do?” (e.g. controller = HTTP only, service = no SQL).

This keeps your code aligned with the architecture and shows clear software engineering thinking.
