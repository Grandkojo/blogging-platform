package com.blogging_platform.classes;

import java.time.LocalDateTime;

public record PostRecordF(
    String id,
    String title,
    String content,
    String status,
    String author,
    LocalDateTime publishedDate,
    int commentCount
) {}

