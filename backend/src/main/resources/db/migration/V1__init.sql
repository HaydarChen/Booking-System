-- Inventory: represent flight/hotel inventory (capacity-based)
create table if not exists inventories (
    id bigserial primary key,
    type varchar(20) not null,
    item_code varchar(120) not null,
    total_capacity int not null check (total_capacity >= 0),
    available int not null check (available >= 0),
    version bigint not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_inventories_type_item_code unique (type, item_code)
);

create index if not exists idx_inventories_type on inventories(type);

-- Booking: reserve inventory with expiry + idempotency
create table if not exists bookings (
    id uuid primary key,
    user_id varchar(80) not null,
    inventory_id bigint not null references inventories(id),
    quantity int not null check (quantity > 0),
    status varchar(30) not null,
    expires_at timestamptz null,
    idempotency_key varchar(80) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_bookings_idempotency_key unique (idempotency_key)
);

create index if not exists idx_bookings_inventory_id on bookings(inventory_id);
create index if not exists idx_bookings_status_expires_at on bookings(status, expires_at);

-- Payment: simulated payment for booking
create table if not exists payments (
    id bigserial primary key,
    booking_id uuid not null references bookings(id),
    status varchar(20) not null,
    provider_ref varchar(120) null,
    amount numeric(12,2) not null check (amount >= 0),
    created_at timestamptz not null default now(),
    constraint uq_payments_booking unique (booking_id)
);