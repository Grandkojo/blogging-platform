package com.blogging_platform.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Comment {
    private UUID id;
    private UUID userId;
    private UUID postId;
    private String comment;
    private String metadata;
    private LocalDateTime datetime;
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public UUID getPostId() {
        return postId;
    }
    public void setPostId(UUID postId) {
        this.postId = postId;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getMetadata() {
        return metadata;
    }
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    public LocalDateTime getDatetime() {
        return datetime;
    }
    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

}
