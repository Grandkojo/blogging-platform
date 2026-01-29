package com.blogging_platform.classes;

import java.time.LocalDateTime;

public record ReviewRecord(
    String id,
    String postId,
    String userId,
    String authorName,
    Integer rating,
    String message,
    LocalDateTime createdAt
) {
    /** Constructor without authorName for backward compatibility; authorName will be null. */
    public ReviewRecord(String id, String postId, String userId, Integer rating, String message, LocalDateTime createdAt) {
        this(id, postId, userId, null, rating, message, createdAt);
    }
}
