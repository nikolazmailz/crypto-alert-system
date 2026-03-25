-- liquibase formatted sql
-- changeset crypto-alert-system:create_crypto_prices

create table crypto_prices (
    symbol varchar(20) primary key,
    price decimal(18, 8) not null,
    updated_at timestamp with time zone not null
);

-- rollback drop table crypto_prices;
