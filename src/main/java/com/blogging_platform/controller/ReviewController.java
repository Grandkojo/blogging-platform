package com.blogging_platform.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.Argument;
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
import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.model.Review;
import com.blogging_platform.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * REST and GraphQL controller for reviews (ratings and messages) on posts.
 */
@RestController
@Tag(name = "Reviews", description = "APIs for managing reviews (ratings and messages) on posts")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Creates a controller with the required {@link ReviewService}.
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * GraphQL query that returns all reviews.
     */
    @QueryMapping
    public List<ReviewRecord> getReviewss() {
        return reviewService.getReviews();
    }

    /**
     * GraphQL mutation to create a new review for a post.
     */
    @MutationMapping(name = "createReview")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public Boolean createReviewMutation(
            @Argument String postId,
            @Argument String userId,
            @Argument Integer rating,
            @Argument String message) {
        Review review = new Review(UUID.fromString(postId), UUID.fromString(userId), rating, message);
        reviewService.createReview(review);
        return true;
    }

    /**
     * GraphQL mutation to update an existing review.
     */
    @MutationMapping(name = "updateReview")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public Boolean updateReviewMutation(
            @Argument String id,
            @Argument String postId,
            @Argument String userId,
            @Argument Integer rating,
            @Argument String message) {
        Review review = new Review(id, UUID.fromString(postId), UUID.fromString(userId), rating, message);
        reviewService.updateReview(review);
        return true;
    }

    /**
     * GraphQL mutation to delete a review for a given user.
     */
    @MutationMapping(name = "deleteReview")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public Boolean deleteReviewMutation(
            @Argument String userId,
            @Argument String id) {
        reviewService.deleteReview(id, userId);
        return true;
    }

    @Operation(summary = "List reviews", description = "Returns all reviews across all posts.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Object>> getReviews() {
        List<ReviewRecord> reviews = reviewService.getReviews();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, reviews, "Reviews Fetched Successfully"));
    }

    @Operation(summary = "Get review by id", description = "Fetches a single review by its id.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Object>> getReview(@PathVariable String id) {
        ReviewRecord review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, review, "Review Found Successfully"));
    }

    @Operation(summary = "List reviews for post", description = "Returns all reviews for the specified post.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Post reviews fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/posts/{postId}/reviews")
    public ResponseEntity<ApiResponse<Object>> getPostReviews(@PathVariable String postId) {
        List<ReviewRecord> comments = reviewService.getReviewsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments, "Post Reviews Found Successfully"));
    }

    @Operation(summary = "Create review", description = "Creates a new review for a post. One review per user per post.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Post or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User has already reviewed this post"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public ResponseEntity<ApiResponse<Object>> createReview(@RequestParam String userId, @RequestParam String postId,
            @Valid @RequestBody Review review) {
        review.setUserId(UUID.fromString(userId));
        review.setPostId(UUID.fromString(postId));
        reviewService.createReview(review);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Review Added Successfully"));

    }

    @Operation(summary = "Update review", description = "Updates an existing review.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Post does not match review or missing required fields"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not the author of this review"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review with id not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping("reviews/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public ResponseEntity<ApiResponse<Object>> editReview(@PathVariable String id, @RequestParam String userId,
            @RequestParam String postId, @RequestBody Review review) {
        review.setId(UUID.fromString(id));
        review.setUserId(UUID.fromString(userId));
        review.setPostId(UUID.fromString(postId));
        reviewService.updateReview(review);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Review Updated Successfully"));

    }

    @Operation(summary = "Delete review", description = "Deletes a review for a given user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Review deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/{userId}/reviews/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHOR','READER')")
    public ResponseEntity<ApiResponse<Object>> deleteReview(@PathVariable String userId, @PathVariable String id) {
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Review Deleted Successfully"));
    }

}
