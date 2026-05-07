ALTER TABLE product
    ADD COLUMN reserved_quantity integer NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS product_reservation
(
    id integer not null primary key,
    order_id integer not null,
    order_reference varchar(255) not null,
    product_id integer not null,
    quantity integer not null,
    status varchar(255) not null,
    constraint uk_product_reservation_order_product unique (order_id, product_id)
);

CREATE SEQUENCE IF NOT EXISTS product_reservation_seq increment by 50;
