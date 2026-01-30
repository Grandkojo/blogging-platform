package com.blogging_platform.classes;

import java.time.LocalDateTime;

/**
 * Immutable DTO for a post in a flat/list context (id, title, content, status, author, published date, comment count).
 */
public record PostRecordF(
    String id,
    String title,
    String content,
    String status,
    String author,
    LocalDateTime publishedDate,
    int commentCount
) {}

