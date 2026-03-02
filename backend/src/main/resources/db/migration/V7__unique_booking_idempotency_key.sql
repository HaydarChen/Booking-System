alter table bookings
    alter column idempotency_key set not null;

create unique index if not exists uq_bookings_idempotency_key
on bookings(idempotency_key)