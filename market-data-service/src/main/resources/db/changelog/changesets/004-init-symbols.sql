-- liquibase formatted sql
-- changeset crypto-alert-system:create_symbols

create table symbols (
    symbol          varchar(20)              primary key,
    base_asset      varchar(20)              not null,
    quote_asset     varchar(20)              not null,
    status          varchar(20)              not null,
    last_updated_at timestamp with time zone not null
);

create index idx_symbols_status on symbols (status);

-- rollback drop index idx_symbols_status;
-- rollback drop table symbols;
