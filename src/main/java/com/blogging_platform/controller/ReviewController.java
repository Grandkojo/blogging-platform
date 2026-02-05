package com.blogging_platform.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.model.Review;
import com.blogging_platform.service.ReviewService;

import jakarta.validation.Valid;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @QueryMapping
    public List<ReviewRecord> getReviewss(){
        return reviewService.getReviews();
    }

    @MutationMapping(name = "createReview")
    public Boolean createReviewMutation(
        @Argument String postId,
        @Argument String userId,
        @Argument Integer rating,
        @Argument String message
    ) {
        Review review = new Review(postId, userId, rating, message);
        reviewService.createReview(review);
        return true;
    }

    @MutationMapping(name = "updateReview")
    public Boolean updateReviewMutation(
        @Argument String id,
        @Argument String postId,
        @Argument String userId,
        @Argument Integer rating,
        @Argument String message
    ) {
        Review review = new Review(id, postId, userId, rating, message);
        reviewService.updateReview(review);
        return true;
    }

    @MutationMapping(name = "deleteReview")
    public Boolean deleteReviewMutation(
        @Argument String userId,
        @Argument String id
    ) {
        reviewService.deleteReview(id, userId);
        return true;
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Object>> getReviews() {
        List<ReviewRecord> reviews = reviewService.getReviews();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, reviews, "Reviews Fetched Successfully"));
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Object>> getReview(@PathVariable String id) {
        ReviewRecord review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, review , "Review Found Successfully"));
    }

    @GetMapping("/posts/{postId}/reviews")
    public ResponseEntity<ApiResponse<Object>> getPostReviews(@PathVariable String postId) {
        List<ReviewRecord> comments = reviewService.getReviewsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, comments , "Post Reviews Found Successfully"));
    }
    

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<Object>> createReview(@Valid @RequestBody Review review) {
        reviewService.createReview(review);  
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Review Added Successfully"));
      
    }

    @PutMapping("reviews/{id}")
    public ResponseEntity<ApiResponse<Object>> editReview(@PathVariable String id, @RequestBody Review review) {
        review.setId(id);
        reviewService.updateReview(review);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.CREATED, null, "Review Updated Successfully"));

    }

    @DeleteMapping("/{userId}/reviews/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteReview(@PathVariable String userId, @PathVariable String id) {
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Review Deleted Successfully"));
    }
    
}
