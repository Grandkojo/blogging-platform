package com.blogging_platform.classes;

import java.time.LocalDateTime;

/**
 * Immutable data transfer object for a blog post as returned by the DAO layer.
 * Contains id, title, content, status, author, timestamps, comment count, and optional user id.
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
    String userId
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
        this(id, title, content, status, author, createdAt, publishedDate, null, null);
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
        this(id, title, content, status, author, createdAt, publishedDate, null, userId);
    }

    public PostRecord {
        // If null is passed, default it to 0
        if (commentCount == null) {
            commentCount = 0;
        } else if (userId == null){
            userId = "";
        } else {
            commentCount = 0;
            userId = null;
        }
    }
}
