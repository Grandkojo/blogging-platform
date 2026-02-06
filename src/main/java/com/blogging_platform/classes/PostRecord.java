package com.blogging_platform.classes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable data transfer object for a blog post as returned by the DAO layer.
 * Contains id, title, content, status, author, timestamps, comment count, optional user id, and tags.
 */
public record PostRecord(
    String id,
    String title,
    String content,
    String status,
    String author,
    LocalDateTime createdAt,
    LocalDateTime publishedDate,
    Integer commentCount,
    String userId,
    List<String> tags
) {

    /**
     * Constructor without comment count or user id (e.g. for list views).
     */
    public PostRecord(
        String id,
        String title,
        String content,
        String status,
        String author,
        LocalDateTime createdAt,
        LocalDateTime publishedDate
    ) {
        this(id, title, content, status, author, createdAt, publishedDate, null, null, null);
    }

    /**
     * Constructor with user id but without comment count.
     */
    public PostRecord(
        String id,
        String title,
        String content,
        String status,
        String author,
        LocalDateTime createdAt,
        LocalDateTime publishedDate,
        String userId
    ) {
        this(id, title, content, status, author, createdAt, publishedDate, null, userId, null);
    }

    public PostRecord {
        // Default commentCount to 0 if null; leave userId as provided; default tags to empty list.
        if (commentCount == null) {
            commentCount = 0;
        }
        if (tags == null) {
            tags = List.of();
        }
    }
}
