package com.blogging_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.model.Review;
import com.blogging_platform.service.ReviewService;

class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private ReviewController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ReviewController(reviewService);
    }

    @Test
    void getReviews_rest_returnsAllReviews() {
        List<ReviewRecord> records = List.of(
                new ReviewRecord("r1", "p1", "u1", "Author", 5, "Great", null));
        when(reviewService.getReviews()).thenReturn(records);

        ResponseEntity<ApiResponse<Object>> response = controller.getReviews();

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Reviews Fetched Successfully", response.getBody().getMessage());
        assertEquals(records, response.getBody().getData());
        verify(reviewService).getReviews();
    }

    @Test
    void getReview_rest_returnsSingleReview() {
        ReviewRecord record = new ReviewRecord("r1", "p1", "u1", "Author", 5, "Great", null);
        when(reviewService.getReviewById("r1")).thenReturn(record);

        ResponseEntity<ApiResponse<Object>> response = controller.getReview("r1");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Review Found Successfully", response.getBody().getMessage());
        assertEquals(record, response.getBody().getData());
        verify(reviewService).getReviewById("r1");
    }

    @Test
    void getPostReviews_rest_returnsReviewsForPost() {
        List<ReviewRecord> records = List.of(
                new ReviewRecord("r1", "p1", "u1", "Author", 5, "Great", null));
        when(reviewService.getReviewsByPostId("p1")).thenReturn(records);

        ResponseEntity<ApiResponse<Object>> response = controller.getPostReviews("p1");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Post Reviews Found Successfully", response.getBody().getMessage());
        assertEquals(records, response.getBody().getData());
        verify(reviewService).getReviewsByPostId("p1");
    }

    @Test
    void createReview_rest_callsServiceAndReturnsCreated() {
        Review review = new Review();
        String userId = java.util.UUID.randomUUID().toString();
        String postId = java.util.UUID.randomUUID().toString();

        ResponseEntity<ApiResponse<Object>> response = controller.createReview(userId, postId, review);

        verify(reviewService).createReview(review);
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
        assertEquals("Review Added Successfully", response.getBody().getMessage());
        assertEquals(java.util.UUID.fromString(userId), review.getUserId());
        assertEquals(java.util.UUID.fromString(postId), review.getPostId());
    }

    @Test
    void editReview_rest_callsServiceAndReturnsUpdated() {
        Review review = new Review();
        String reviewId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();
        String postId = java.util.UUID.randomUUID().toString();

        ResponseEntity<ApiResponse<Object>> response = controller.editReview(reviewId, userId, postId, review);

        verify(reviewService).updateReview(review);
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
        assertEquals("Review Updated Successfully", response.getBody().getMessage());
        assertEquals(java.util.UUID.fromString(userId), review.getUserId());
        assertEquals(java.util.UUID.fromString(postId), review.getPostId());
    }

    @Test
    void deleteReview_rest_callsServiceAndReturnsAccepted() {
        ResponseEntity<ApiResponse<Object>> response = controller.deleteReview("u1", "r1");

        verify(reviewService).deleteReview("r1", "u1");
        assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
        assertEquals("Review Deleted Successfully", response.getBody().getMessage());
    }

    @Test
    void getReviews_graphql_delegatesToService() {
        List<ReviewRecord> records = List.of(
                new ReviewRecord("r1", "p1", "u1", "Author", 5, "Great", null));
        when(reviewService.getReviews()).thenReturn(records);

        List<ReviewRecord> result = controller.getReviewss();

        assertEquals(records, result);
        verify(reviewService).getReviews();
    }

    @Test
    void createReview_graphql_callsServiceAndReturnsTrue() {
        String postId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();

        Boolean result = controller.createReviewMutation(postId, userId, 5, "Great");

        verify(reviewService).createReview(org.mockito.Mockito.any(Review.class));
        assertEquals(true, result);
    }

    @Test
    void updateReview_graphql_callsServiceAndReturnsTrue() {
        String reviewId = java.util.UUID.randomUUID().toString();
        String postId = java.util.UUID.randomUUID().toString();
        String userId = java.util.UUID.randomUUID().toString();

        Boolean result = controller.updateReviewMutation(reviewId, postId, userId, 4, "Good");

        verify(reviewService).updateReview(org.mockito.Mockito.any(Review.class));
        assertEquals(true, result);
    }

    @Test
    void deleteReview_graphql_callsServiceAndReturnsTrue() {
        Boolean result = controller.deleteReviewMutation("u1", "r1");

        verify(reviewService).deleteReview("r1", "u1");
        assertEquals(true, result);
    }
}
