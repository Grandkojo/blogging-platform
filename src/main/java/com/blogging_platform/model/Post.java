package com.blogging_platform.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Domain model for a blog post. Used when creating or updating posts via the service layer.
 */
@Entity
public class Post {
    @Id
    private UUID id;
    private UUID userId;
    private String title;
    private String content;
    private String status;
    private String createdAt;

    private LocalDateTime publishedDatetime;
    private boolean isPublish;

    public Post() {
    }

    public Post(UUID userId, String title, String content, String status) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public Post(UUID postId, UUID userId, String title, String content, String status) {
        this.id = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

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

    public void setIsPublish(boolean b) {
        this.isPublish = b;
    }

    public boolean getIsPublish(){
        return this.isPublish;
    }
}
