# --- !Ups

CREATE TABLE "category"
(
    "id"   INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL
);

CREATE TABLE "product"
(
    "id"          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"        VARCHAR NOT NULL,
    "description" TEXT    NOT NULL,
    "category"    INT     NOT NULL,
    "producer"    INT     NOT NULL,
    "price"       INT     NOT NULL,

    FOREIGN KEY (category) REFERENCES category (id),
    FOREIGN KEY (producer) REFERENCES category (id)
);

CREATE TABLE "producer"
(
    "id"      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"    VARCHAR NOT NULL,
    "address" INT     NOT NULL,

    FOREIGN KEY (address) REFERENCES address (id)
);

CREATE TABLE "address"
(
    "id"           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "city"         VARCHAR NOT NULL,
    "street_name"  VARCHAR NOT NULL,
    "house_number" INT     NOT NULL,
    "postal_code"  VARCHAR NOT NULL
);

CREATE TABLE "user"
(
    "id"        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "user_name" VARCHAR NOT NULL,
    "password"  VARCHAR NOT NULL,
    "email"     VARCHAR NOT NULL,
    "client"    INT,

    FOREIGN KEY (client) REFERENCES client (id)
);

CREATE TABLE "client"
(
    "id"        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name"      VARCHAR NOT NULL,
    "last_name" VARCHAR NOT NULL,
    "address"   INT     NOT NULL,
    "cart"      INT,

    FOREIGN KEY (address) REFERENCES address (id),
    FOREIGN KEY (cart) REFERENCES cart (id)
);

CREATE TABLE "cart"
(
    "id"    INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "value" NUMERIC
);

CREATE TABLE "cart_products"
(
    "id"      INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "cart"    INT     NOT NULL,
    "product" INT     NOT NULL,

    FOREIGN KEY (cart) REFERENCES cart (id),
    FOREIGN KEY (product) REFERENCES cart (id)
);

CREATE TABLE "order"
(
    "id"        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "reference" VARCHAR NOT NULL
);

CREATE TABLE "client_orders"
(
    "id"     INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "client" INT     NOT NULL,
    "order"  INT     NOT NULL,

    FOREIGN KEY (client) REFERENCES client (id),
    FOREIGN KEY ("order") REFERENCES "order" (id)
);

CREATE TABLE "discount"
(
    "id"         INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "product"    INT     NOT NULL,
    "percentage" INT     NOT NULL,

    FOREIGN KEY (product) REFERENCES product (id)
);

# --- !Downs

DROP TABLE "category";
DROP TABLE "product";
DROP TABLE "producer";
DROP TABLE "address";
DROP TABLE "user";
DROP TABLE "client";
DROP TABLE "cart";
DROP TABLE "cart_products";
DROP TABLE "order";
DROP TABLE "client_orders";
DROP TABLE "discount";