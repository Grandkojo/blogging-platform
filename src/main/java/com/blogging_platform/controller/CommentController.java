package com.blogging_platform.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
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

/**
 * REST and GraphQL controller for comments on posts.
 * <p>
 * Supports listing, creating, updating and deleting comments as both REST endpoints
 * and GraphQL mutations/queries.
 */
@RestController
public class CommentController {
    
    private final CommentService commentService;

    /**
     * Creates a controller with the required {@link CommentService}.
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * GraphQL query that returns all comments.
     */
    @QueryMapping
    public List<CommentRecord> getCommentss() {
        return commentService.getComments();
    }

    /**
     * REST endpoint returning all comments.
     */
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> getComments() {
        List<CommentRecord> comments = commentService.getComments();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments, "Comments Fetched Successfully"));
    }

    /**
     * REST endpoint returning a single comment by id.
     */
    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Object>> getComment(@PathVariable String id) {
        CommentRecord comment = commentService.getComment(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comment , "Comment Found Successfully"));
    }

    /**
     * GraphQL query that returns a single comment by id.
     */
    @QueryMapping
    public CommentRecord getCommentt(@Argument String id) {
        return commentService.getComment(id);
    }    

    /**
     * REST endpoint returning all comments for a given post.
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Object>> getPostComments(@PathVariable String postId) {
        List<CommentRecord> comments = commentService.getComments(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments , "Post Comments Found Successfully"));
    }

    /**
     * GraphQL query that returns all comments for a given post.
     */
    @QueryMapping
    public List<CommentRecord> getPostCommentss(@Argument String postId) {
        return commentService.getComments(postId);
    } 
    
    /**
     * GraphQL mutation to create a new comment on a post.
     */
    @MutationMapping(name = "createComment")
    public Boolean createCommentMutation(
        @Argument String userId,
        @Argument String postId,
        @Argument String content
    ) {
        Comment comment = Comment.forCreate(content, userId, postId);
        commentService.addComment(comment);
        return true;
    }

    /**
     * GraphQL mutation to update an existing comment.
     */
    @MutationMapping(name = "updateComment")
    public Boolean updateCommentMutation(
        @Argument String id,
        @Argument String userId,
        @Argument String content
    ) {
        Comment comment = Comment.forEdit(id, userId, content);
        commentService.editComment(comment);
        return true;
    }

    /**
     * GraphQL mutation to delete a comment for a given user.
     */
    @MutationMapping(name = "deleteComment")
    public Boolean deleteCommentMutation(
        @Argument String userId,
        @Argument String id
    ) {
        commentService.deleteComment(id, userId);
        return true;
    }

    /**
     * REST endpoint to create a new comment.
     */
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> createComment(@Valid @RequestBody Comment comment) {
        commentService.addComment(comment);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Added Successfully"));
      
    }

    /**
     * REST endpoint to update an existing comment.
     */
    @PutMapping("comments/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @RequestBody Comment comment) {
        comment.setId(id);
        commentService.editComment(comment);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Updated Successfully"));

    }

    /**
     * REST endpoint to delete a comment for a given user.
     */
    @DeleteMapping("/{userId}/comments/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Comment Deleted Successfully"));
    }
}
