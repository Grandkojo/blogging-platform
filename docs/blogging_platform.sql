CREATE TABLE `users` (
  `id` uuid PRIMARY KEY,
  `name` varchar(255),
  `email` varchar(255) UNIQUE,
  `role` ENUM ('Admin', 'Regular')
);

CREATE TABLE `posts` (
  `id` uuid PRIMARY KEY,
  `user_id` uuid NOT NULL,
  `title` varchar(255) UNIQUE,
  `content` varchar(255),
  `status` ENUM ('DRAFT', 'PUBLISHED', 'DELETED'),
  `published_datetime` datetime
);

CREATE TABLE `comments` (
  `id` uuid PRIMARY KEY,
  `user_id` uuid,
  `post_id` uuid,
  `comment` varchar(255),
  `metadata` json,
  `datetime` datetime
);

CREATE TABLE `tags` (
  `id` uuid PRIMARY KEY,
  `tag` varchar(255)
);

CREATE TABLE `post_tags` (
  `post_id` uuid NOT NULL,
  `tag_id` uuid NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`),
  PRIMARY KEY (`post_id`, `tag_id`)
);

CREATE UNIQUE INDEX `users_index_0` ON `users` (`name`);

CREATE UNIQUE INDEX `posts_index_1` ON `posts` (`title`);

CREATE INDEX `posts_index_2` ON `posts` (`user_id`);

CREATE INDEX `posts_index_3` ON `posts` (`published_datetime`);

CREATE INDEX `comments_index_4` ON `comments` (`user_id`);

CREATE INDEX `comments_index_5` ON `comments` (`post_id`);

CREATE INDEX `comments_index_6` ON `comments` (`datetime`);

CREATE UNIQUE INDEX `tags_index_7` ON `tags` (`tag`);

ALTER TABLE `posts` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `comments` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `comments` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

ALTER TABLE `post_tags` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);

ALTER TABLE `post_tags` ADD FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`);