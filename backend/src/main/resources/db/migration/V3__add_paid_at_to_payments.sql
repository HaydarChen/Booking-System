alter table payments
add column if not exists paid_at timestamptz;