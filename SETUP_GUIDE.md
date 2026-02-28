# Setup Guide — Do It Yourself

Follow these steps in order. Execute every command and create every file yourself.

---

## Step 0: Prerequisites

Check that you have these installed:

1. **Java 17 or 21**  
   - Run: `java -version`  
   - If missing: install OpenJDK (e.g. from [Adoptium](https://adoptium.net/) or your OS package manager).

2. **Maven 3.6+** (or Gradle)  
   - Run: `mvn -v`  
   - If missing: install Maven from [maven.apache.org](https://maven.apache.org/download.cgi) or SDKMAN/Homebrew.

3. **Docker and Docker Compose**  
   - Run: `docker --version` and `docker compose version`  
   - If missing: install [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Compose).

4. **Git** (optional but useful)  
   - Run: `git --version`

---

## Step 1: Create the Spring Boot Project

**Option A — Using start.spring.io (recommended)**

1. Open: [https://start.spring.io](https://start.spring.io)
2. Set:
   - **Project:** Maven  
   - **Language:** Java  
   - **Spring Boot:** 3.2.x or 3.3.x (latest stable)  
   - **Group:** `com.yourname` (e.g. `com.john.booking`)  
   - **Artifact:** `booking-system`  
   - **Name:** `booking-system`  
   - **Package name:** `com.yourname.booking`  
   - **Packaging:** Jar  
   - **Java:** 17 or 21  
3. Click **Add Dependencies** and add:
   - Spring Web  
   - Spring Data JPA  
   - Spring Data Redis  
   - PostgreSQL Driver  
   - Validation  
   - Spring Boot Actuator (for health checks)  
   - Lombok (optional, for less boilerplate)  
4. Click **Generate**, download the ZIP, and extract it into your `booking-system` folder (merge with existing files like README.md and LEARNING_GUIDE.md; keep those files).

**Option B — Using curl**

Penting: jalankan dari folder **di atas** `booking-system` (misalnya `personal-projects`), bukan dari dalam `booking-system`. Kalau dijalankan dari dalam `booking-system`, ZIP akan menimpa/bercampur dengan file yang sudah ada.

```bash
cd /Users/macbookpro/Documents/personal-projects
curl -o booking-system.zip "https://start.spring.io/starter.zip?type=maven-project&language=java&groupId=com.yourname&artifactId=booking-system&name=booking-system&packageName=com.yourname.booking&javaVersion=17&dependencies=web,data-jpa,data-redis,postgresql,validation,actuator"
```

Jangan pakai `bootVersion=3.2.5` — versi itu bisa tidak lagi didukung, sehingga server mengembalikan error (file kecil ~422 byte, bukan ZIP). Tanpa `bootVersion` server pakai versi terbaru.

Cek dulu bahwa file benar-benar ZIP (biasanya puluhan ribu byte):

```bash
ls -la booking-system.zip
file booking-system.zip
```

Kalau keluar `Zip archive data` dan ukuran besar (misalnya 50KB+), lanjut unzip:

```bash
unzip booking-system.zip -d booking-system-temp
# Pindahkan isi booking-system-temp ke folder booking-system Anda, lalu hapus booking-system-temp
```

Kalau file kecil atau bukan ZIP, buka isinya: `cat booking-system.zip` — itu pesan error dari server. Gunakan Option A (browser) saja.

---

## Step 2: Confirm Project Layout

Put the Spring Boot project inside a **`backend/`** folder so the root has both `backend/` and `frontend/`:

- `backend/pom.xml` (or `build.gradle` if you used Gradle)  
- `backend/src/main/java/com/yourname/booking/`  
- `backend/src/main/resources/`  
- `backend/src/test/java/`  

Your guides (README, LEARNING_GUIDE, PROJECT_STRUCTURE, SETUP_GUIDE) and `docker-compose.yml` stay at the **root** of `booking-system` (same level as `backend/` and `frontend/`).

---

## Step 3: Docker Compose for PostgreSQL and Redis

In the **root** of `booking-system` (same folder as `backend/` and `frontend/`), create a file named **`docker-compose.yml`**.

Put this inside (you can type it or copy-paste):

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: booking-postgres
    environment:
      POSTGRES_DB: booking_db
      POSTGRES_USER: booking_user
      POSTGRES_PASSWORD: booking_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U booking_user -d booking_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: booking-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

Save the file.

---

## Step 4: Start PostgreSQL and Redis

From the **root** of `booking-system` run:

```bash
docker compose up -d
```

Check that both containers are running:

```bash
docker compose ps
```

Both `booking-postgres` and `booking-redis` should be **Up**.  
Optional: run `docker compose logs -f` to see logs; press Ctrl+C to stop following.

---

## Step 5: Application Configuration

Open (or create) **`backend/src/main/resources/application.yml`**.

Replace or set its content to the following (indent with spaces, 2 spaces per level):

```yaml
spring:
  application:
    name: booking-system

  datasource:
    url: jdbc:postgresql://localhost:5432/booking_db
    username: booking_user
    password: booking_pass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect

  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

Save the file.

- **Explanation:** DB and Redis point to the Docker services. `ddl-auto: update` lets JPA create/update tables from your entities (fine for learning; in production you’d use migrations).

---

## Step 6: Create Package Structure

In **`backend/src/main/java/com/yourname/booking/`** create these **packages** (empty is fine; you can add a dummy class and delete it later if your IDE requires a file):

- `config`  
- `controller`  
- `dto`  
- `entity`  
- `exception`  
- `repository`  
- `service`  

You can do it by creating folders, or in your IDE: right‑click the `booking` package → New → Package, and add each name.

Your structure should look like:

```
backend/src/main/java/com/yourname/booking/
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── service
└── BookingSystemApplication.java   (or similar — from Spring Initializr)
```

---

## Step 7: Verify the App Starts

1. Make sure Docker is still running and PostgreSQL + Redis are up:  
   `docker compose ps`  
2. From the **root** of `booking-system` run:

```bash
cd backend && ./mvnw spring-boot:run
```

(or Gradle: `./gradlew bootRun`)

3. Wait until you see something like “Started BookingSystemApplication in X seconds”.  
4. In another terminal (or browser) run:

```bash
curl http://localhost:8080/actuator/health
```

You should get JSON with `"status":"UP"`.  
5. Stop the app with **Ctrl+C** in the terminal where it’s running.

---

## Step 8: Optional — Quick Health Check for DB and Redis

To confirm the app talks to PostgreSQL and Redis, you can add a simple health indicator or just rely on Actuator.  
For now, **Step 7 is enough** — if the app starts and actuator health is UP, your setup is good.

Later you can enable `show-details: always` for `/actuator/health` to see DB and Redis status when you add the corresponding dependencies and config.

---

## Checklist

Before moving on to implementing features (from PROJECT_STRUCTURE.md and LEARNING_GUIDE.md), confirm:

- [ ] Java 17/21 and Maven (or Gradle) installed  
- [ ] Docker and Docker Compose installed  
- [ ] Spring Boot project created and dependencies added  
- [ ] `docker-compose.yml` created and PostgreSQL + Redis started  
- [ ] `backend/src/main/resources/application.yml` configured with DB and Redis  
- [ ] Packages created under `backend/src/main/java/com/yourname/booking/`  
- [ ] `cd backend && ./mvnw spring-boot:run` (or `./gradlew bootRun`) starts the app  
- [ ] `curl http://localhost:8080/actuator/health` returns `"status":"UP"`  

If any step fails, re-read that step, check paths and ports (5432, 6379, 8080), and that Docker containers are running.  
Next: follow **PROJECT_STRUCTURE.md** and **LEARNING_GUIDE.md** to implement entities, repositories, services, and controllers.
