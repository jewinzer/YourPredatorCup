# --- !Ups

CREATE TABLE "CTCH" ("ID" SERIAL PRIMARY KEY,
                        "NAME" VARCHAR NOT NULL,
                      "SPECIES" VARCHAR NOT NULL,
                        "LENGTH" INTEGER NOT NULL);

# --- !Downs

DROP TABLE "CTCH";
