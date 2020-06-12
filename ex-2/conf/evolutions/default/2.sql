# --- !Ups

INSERT INTO "category"("id", "name") VALUES(0, "weights");
INSERT INTO "category"("id", "name") VALUES(1, "accessories");
INSERT INTO "category"("id", "name") VALUES(2, "wear");
INSERT INTO "category"("id", "name") VALUES(3, "supplements");

INSERT INTO "address"("id", "city", "street_name", "house_number", "postal_code") VALUES(0, "Opole", "Gryczana", 9, "45-234");

INSERT INTO "producer"("id", "name", "address") VALUES(0, "Eleiko", 0);

INSERT INTO "product"("name", "description", "category", "producer", "price") VALUES("Powerlifting barbell", "20kg 220cm barbell", "weights", "Eleiko", 2999);

# --- !Downs