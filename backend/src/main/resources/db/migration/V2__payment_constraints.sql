alter table payments
add constraint uq_payments_provider_ref unique (provider_ref);