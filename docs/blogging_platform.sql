CREATE TABLE `users` (
  `id` BINARY(16) PRIMARY KEY,
  `name` VARCHAR(255),
  `email` VARCHAR(255) UNIQUE,
  `role` ENUM('Admin', 'Regular') NOT NULL
);

CREATE TABLE `posts` (
  `id` BINARY(16) PRIMARY KEY,
  `user_id` BINARY(16) NOT NULL,
  `title` VARCHAR(255) UNIQUE,
  `content` TEXT,
  `status` ENUM('DRAFT', 'PUBLISHED', 'DELETED'),
  `published_datetime` DATETIME,
  `published_datetime` DATETIME
);

CREATE TABLE `comments` (
  `id` BINARY(16) PRIMARY KEY,
  `user_id` BINARY(16),
  `post_id` BINARY(16),
  `comment` VARCHAR(255),
  `metadata` JSON,
  `datetime` DATETIME
);

CREATE TABLE `tags` (
  `id` BINARY(16) PRIMARY KEY,
  `tag` VARCHAR(255)
);

CREATE TABLE `post_tags` (
  `post_id` BINARY(16) NOT NULL,
  `tag_id` BINARY(16) NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`)
);

CREATE TABLE `reviews` (
  `id` BINARY(16) PRIMARY KEY,
  `post_id` BINARY(16) NOT NULL,
  `user_id` BINARY(16) NOT NULL,
  `rating` TINYINT NOT NULL,
  `message` VARCHAR(255),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_user_review` (`post_id`, `user_id`)
);


-- Indexes
CREATE UNIQUE INDEX `users_index_0` ON `users` (`name`);
CREATE UNIQUE INDEX `posts_index_1` ON `posts` (`title`);
CREATE INDEX `posts_index_2` ON `posts` (`user_id`);
CREATE INDEX `posts_index_3` ON `posts` (`published_datetime`);
CREATE INDEX `comments_index_4` ON `comments` (`user_id`);
CREATE INDEX `comments_index_5` ON `comments` (`post_id`);
CREATE INDEX `comments_index_6` ON `comments` (`datetime`);
CREATE UNIQUE INDEX `tags_index_7` ON `tags` (`tag`);

-- Foreign Keys
ALTER TABLE `posts` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `comments` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `comments` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);
ALTER TABLE `post_tags` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);
ALTER TABLE `post_tags` ADD FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`);
ALTER TABLE `reviews` ADD FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`);
ALTER TABLE `reviews` ADD FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- uuid aut generation
-- For users table
ALTER TABLE users 
MODIFY COLUMN id BINARY(16) DEFAULT (UUID_TO_BIN(UUID()));

-- For posts table
ALTER TABLE posts 
MODIFY COLUMN id BINARY(16) DEFAULT (UUID_TO_BIN(UUID()));

-- For comments table
ALTER TABLE comments 
MODIFY COLUMN id BINARY(16) DEFAULT (UUID_TO_BIN(UUID()));

-- For tags table
ALTER TABLE tags 
MODIFY COLUMN id BINARY(16) DEFAULT (UUID_TO_BIN(UUID()));

-- For reviews table
ALTER TABLE reviews 
MODIFY COLUMN id BINARY(16) DEFAULT (UUID_TO_BIN(UUID()));
-- add checks for rating 
ALTER TABLE `reviews` ADD CONSTRAINT chk_rating CHECK (rating >= 1 AND rating <= 5);


ALTER TABLE users 
ADD COLUMN password VARCHAR(255) NOT NULL;


-- for faster title search and author names search, case-insensitive
CREATE FULLTEXT INDEX idx_title_fulltext ON posts(title);
CREATE FULLTEXT INDEX idx_author_fulltext ON users(name);