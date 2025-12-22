CREATE TYPE "user_role" AS ENUM (
  'Admin',
  'Regular'
);

CREATE TYPE "post_status" AS ENUM (
  'DRAFT',
  'PUBLISHED',
  'DELETED'
);

CREATE TABLE "users" (
  "id" uuid PRIMARY KEY,
  "name" varchar,
  "email" varchar UNIQUE,
  "role" user_role
);

CREATE TABLE "posts" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid NOT NULL,
  "title" varchar UNIQUE,
  "content" varchar,
  "status" post_status,
  "published_datetime" datetime
);

CREATE TABLE "comments" (
  "id" uuid PRIMARY KEY,
  "user_id" uuid,
  "post_id" uuid,
  "comment" varchar,
  "datetime" datetime
);

CREATE TABLE "tags" (
  "id" uuid PRIMARY KEY,
  "tag" varchar
);

CREATE TABLE "post_tags" (
  "post_id" uuid NOT NULL,
  "tag_id" uuid NOT NULL,
  PRIMARY KEY ("post_id", "tag_id"),
  PRIMARY KEY ("post_id", "tag_id")
);

CREATE UNIQUE INDEX ON "users" ("name");

CREATE UNIQUE INDEX ON "posts" ("title");

CREATE INDEX ON "posts" ("user_id");

CREATE INDEX ON "comments" ("user_id");

CREATE INDEX ON "comments" ("post_id");

CREATE UNIQUE INDEX ON "tags" ("tag");

ALTER TABLE "posts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "comments" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id");

ALTER TABLE "comments" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "post_tags" ADD FOREIGN KEY ("post_id") REFERENCES "posts" ("id");

ALTER TABLE "post_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "tags" ("id");
