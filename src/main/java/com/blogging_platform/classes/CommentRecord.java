package com.blogging_platform.classes;

import java.time.LocalDateTime;

public record CommentRecord(
    String id,
    String postId,
    String userId,
    String authorName,
    String content,
    LocalDateTime date
 ) {}
