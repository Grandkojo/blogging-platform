package com.blogging_platform.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.model.Comment;
import com.blogging_platform.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST and GraphQL controller for comments on posts.
 * <p>
 * Supports listing, creating, updating and deleting comments as both REST endpoints
 * and GraphQL mutations/queries.
 */
@RestController
@Tag(name = "Comments", description = "APIs for managing comments on posts")
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
    public List<CommentRecord> getCommentss(
        @Argument Integer page,
        @Argument Integer size,
        @Argument String sortBy,
        @Argument String dir
    ) {
        int p = page != null ? page : 0;
        int s = size != null ? size : 10;
        String direction = dir != null ? dir : "DESC";

        Sort sort = Sort.by(
            Sort.Direction.fromString(direction),
            sortBy
        );
        Pageable pagination = PageRequest.of(p, s, sort);
        return commentService.getComments(pagination);
    }

    @Operation(
        summary = "List comments",
        description = "Returns all comments across all posts."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comments fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> getComments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false, defaultValue = "datetime") String sortBy,
        @RequestParam(required = false, defaultValue = "DESC") String dir

    ) {
        Sort sort = Sort.by(
            Sort.Direction.fromString(dir),
            sortBy
        );
        Pageable pagination = PageRequest.of(page, size, sort);
        List<CommentRecord> comments = commentService.getComments(pagination);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments, "Comments Fetched Successfully"));
    }

    @Operation(
        summary = "Get comment by id",
        description = "Fetches a single comment by its id."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Comment found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
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

    @Operation(
        summary = "List comments for post",
        description = "Returns all comments for the specified post."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post comments fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
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

    @Operation(
        summary = "Create comment",
        description = "Creates a new comment on a post."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post or user not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<Object>> createComment(@Valid @RequestBody Comment comment) {
        commentService.addComment(comment);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Added Successfully"));
      
    }

    @Operation(
        summary = "Update comment",
        description = "Updates an existing comment. Only the author may update."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Comment updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found or user not author"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("comments/{id}")
    public ResponseEntity<ApiResponse<Object>> editPost(@PathVariable String id, @RequestBody Comment comment) {
        comment.setId(UUID.fromString(id));
        commentService.editComment(comment);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Comment Updated Successfully"));

    }

    @Operation(
        summary = "Delete comment",
        description = "Deletes a comment for a given user. Only the author may delete."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Comment deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Comment not found or user not author"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/{userId}/comments/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String userId, @PathVariable String id) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Comment Deleted Successfully"));
    }
}
