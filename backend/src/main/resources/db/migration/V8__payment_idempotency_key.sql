alter table payments
    add column if not exists idempotency_key varchar(120);

create unique index if not exists uq_payments_idempotency_key
    on payments(idempotency_key);

create unique index if not exists uq_payments_booking_id on payments(booking_id);
create unique index if not exists uq_payments_provider_ref on payments(provider_ref);