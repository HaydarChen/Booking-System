create index if not exists idx_booking_status_expires_at
on bookings(status, expires_at)