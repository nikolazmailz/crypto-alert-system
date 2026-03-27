-- liquibase formatted sql
-- changeset crypto-alert-system:create_portfolios

create table portfolios (
    id uuid primary key,
    user_id uuid not null unique,
    -- Храним активы как JSONB массив: [{"symbol": "BTC", "quantity": 0.5}, ...]
    assets jsonb not null default '[]'::jsonb,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_portfolios_user_id on portfolios(user_id);
-- Индекс для быстрого поиска по символу внутри JSON (если понадобится)
create index idx_portfolios_assets_gin on portfolios using gin (assets);
