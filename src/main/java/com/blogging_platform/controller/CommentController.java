package com.blogging_platform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.model.Comment;
import com.blogging_platform.service.CommentService;

import jakarta.validation.Valid;

@RestController
public class CommentController {
    
    private final CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> getComments() {
        List<CommentRecord> comments = commentService.getComments();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments, "Comments Fetched Successfully"));
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Object>> getComment(@PathVariable String id) {
        CommentRecord comment = commentService.getComment(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comment , "Comment Found Successfully"));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Object>> getPostComments(@PathVariable String postId) {
        List<CommentRecord> comments = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments , "Post Comments Found Successfully"));
    }
    

    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> createComment(@Valid @RequestBody Comment comment) {
        commentService.addComment(comment);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Added Successfully"));
      
    }

    @PutMapping("comments/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @RequestBody Comment comment) {
        comment.setId(id);
        commentService.editComment(comment);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Updated Successfully"));

    }

    @DeleteMapping("/{userId}/comments/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Comment Deleted Successfully"));
    }
}
