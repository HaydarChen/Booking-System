/**
 * Base URL for the Spring Boot backend.
 * Set NEXT_PUBLIC_API_URL in .env.local (see .env.example).
 */
const getBaseUrl = (): string => {
  if (typeof window !== "undefined") {
    return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  }
  return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
};

export const API_BASE_URL = getBaseUrl();

/**
 * Build full API path (e.g. /flights/search -> http://localhost:8080/flights/search).
 */
export function apiPath(path: string): string {
  const base = API_BASE_URL.replace(/\/$/, "");
  const p = path.startsWith("/") ? path : `/${path}`;
  return `${base}${p}`;
}
