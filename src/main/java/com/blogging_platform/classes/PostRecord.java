package com.blogging_platform.classes;

import java.time.LocalDateTime;

public record PostRecord(
    String id,
    String title,
    String status,
    String author,
    LocalDateTime publishedDate
) {}
