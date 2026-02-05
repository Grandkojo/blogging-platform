package com.blogging_platform.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Domain model for a post review (rating 1–5 and message). Used when creating or updating reviews.
 */
@Entity
public class Review {
    @Id
    private String id;

    @NotBlank(message = "Post id is required")
    private String postId;

    @NotBlank(message = "User id is required")
    private String userId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")    
    private Integer rating;

    @NotBlank(message = "Review message is required")
    private String message;
    private LocalDateTime createdAt;

    public Review(){}

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
