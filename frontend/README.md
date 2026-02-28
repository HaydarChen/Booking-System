# Frontend — Booking System

Next.js frontend for the Scalable Booking System. Consumes the Spring Boot REST API.

## Prerequisites

- Node.js 18+
- npm or yarn

## Setup

1. **Install dependencies** (already done if you ran `create-next-app`):

   ```bash
   npm install
   ```

2. **Configure backend URL**

   Copy the example env file and set the API base URL:

   ```bash
   cp .env.example .env.local
   ```

   Edit `.env.local` if your backend runs on another host/port:

   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```

## Run

- **Development** (with hot reload):

  ```bash
  npm run dev
  ```

  Open [http://localhost:3000](http://localhost:3000).

- **Production build**:

  ```bash
  npm run build
  npm start
  ```

## Tech

- **Next.js** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **ESLint**

## Project layout

- `src/app/` — App Router pages and layouts
- `src/lib/` — Shared utilities (e.g. API client)
- `src/components/` — Reusable UI (add as you build)

To call the backend, use `NEXT_PUBLIC_API_URL` (e.g. in `src/lib/api.ts`) so all API requests go to the Spring Boot app.
