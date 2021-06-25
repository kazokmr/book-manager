CREATE TYPE ROLE_TYPE AS ENUM ('ADMIN','USER');

CREATE TABLE IF NOT EXISTS account
(
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(32) UNIQUE NOT NULL,
    password  VARCHAR(128)       NOT NULL,
    name      VARCHAR(32)        NOT NULL,
    role_type ROLE_TYPE
);

CREATE TABLE IF NOT EXISTS book
(
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(128) NOT NULL,
    author       VARCHAR(32)  NOT NULL,
    release_date date         NOT NULL
);

CREATE TABLE IF NOT EXISTS rental
(
    book_id         BIGSERIAL PRIMARY KEY,
    account_id      BIGSERIAL NOT NULL,
    rental_datetime TIMESTAMP NOT NULL,
    return_deadline TIMESTAMP NOT NULL
);
