package com.blogging_platform.model;

import java.time.LocalDateTime;

public class Review {
    private String id;
    private String postId;
    private String userId;
    private Integer rating;
    private String message;
    private LocalDateTime createdAt;

    public Review(String postId, String userId, Integer rating, String message) {
        this.postId = postId;
        this.userId = userId;
        this.rating = rating;
        this.message = message;
    }

    public Review(String id, String postId, String userId, Integer rating, String message) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.rating = rating;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
