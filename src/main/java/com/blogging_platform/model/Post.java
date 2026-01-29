package com.blogging_platform.model;

import java.time.LocalDateTime;

public class Post {
    private String id;
    private String userId;
    private String title;
    private String content;
    private String status;
    private String createdAt;

    private LocalDateTime publishedDatetime;
    private boolean isPublish;

    public Post(String userId, String title, String content, String status) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public Post(String postId, String userId, String title, String content, String status) {
        this.id = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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
