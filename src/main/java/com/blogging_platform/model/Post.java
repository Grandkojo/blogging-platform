package com.blogging_platform.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Post {
    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private String status;
    private String createdAt;

    private LocalDateTime publishedDatetime;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedDatetime() {
        return publishedDatetime;
    }

    public void setPublishedDatetime(LocalDateTime publishedDatetime) {
        this.publishedDatetime = publishedDatetime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
