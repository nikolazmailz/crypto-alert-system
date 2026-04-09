-- liquibase formatted sql
-- changeset crypto-alert-system:create_alert

create table alerts (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    symbol varchar(20) not null,
    target_price decimal(38, 18) not null,
    condition varchar(20) not null,
    is_active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp
);

create index idx_alerts_symbol_active on alerts (symbol, is_active);

-- rollback drop index idx_alerts_symbol_active;
-- rollback drop table alerts;
