package com.blogging_platform.classes;

import java.time.LocalDateTime;

/**
 * Immutable data transfer object for a comment on a post.
 * Contains id, post id, user id, author name, content, and timestamp.
 */
public record CommentRecord(
    String id,
    String postId,
    String userId,
    String authorName,
    String content,
    LocalDateTime date
 ) {}
