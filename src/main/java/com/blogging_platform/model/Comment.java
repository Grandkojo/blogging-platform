package com.blogging_platform.model;

import java.time.LocalDateTime;

public class Comment {
    private String id;
    private String userId;
    private String postId;
    private String comment;
    private String metadata;
    private LocalDateTime datetime;

    
    private Comment() {
    }
    
    public static Comment forEdit(String id, String userId, String comment) {
        Comment c = new Comment();
        c.id = id;
        c.userId = userId;
        c.comment = comment;
        return c;
    }
    
    public static Comment forCreate(String comment, String userId, String postId) {
        Comment c = new Comment();
        c.comment = comment;
        c.userId = userId;
        c.postId = postId;
        return c;
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
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
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
