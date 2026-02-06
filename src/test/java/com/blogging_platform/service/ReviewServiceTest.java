package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.dao.interfaces.ReviewDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

/**
 * Unit tests for {@link ReviewService}.
 */
class ReviewServiceTest {

    @Mock
    private ReviewDAO reviewDAO;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewService(reviewDAO);
    }

    @Test
    void createReview_delegatesToDao() throws DatabaseQueryException, DuplicateResourceException {
        Review review = new Review();

        reviewService.createReview(review);

        verify(reviewDAO).create(review);
    }

    @Test
    void createReview_propagatesExceptions() throws DatabaseQueryException, DuplicateResourceException {
        Review review = new Review();
        DuplicateResourceException ex = new DuplicateResourceException("Already reviewed");
        org.mockito.Mockito.doThrow(ex).when(reviewDAO).create(review);

        assertThrows(DuplicateResourceException.class, () -> reviewService.createReview(review));
    }

    @Test
    void getReviewsByPostId_delegatesToDao() throws DatabaseQueryException {
        String postId = "post-1";
        ReviewRecord record = new ReviewRecord("r1", postId, "user-1", "Author", 5, "Great", LocalDateTime.now());
        when(reviewDAO.getReviewsByPostId(postId)).thenReturn(List.of(record));

        List<ReviewRecord> result = reviewService.getReviewsByPostId(postId);

        assertEquals(List.of(record), result);
        verify(reviewDAO).getReviewsByPostId(postId);
    }

    @Test
    void getReviews_delegatesToDao() throws DatabaseQueryException {
        ReviewRecord record = new ReviewRecord("r1", "post-1", "user-1", "Author", 5, "Great", LocalDateTime.now());
        when(reviewDAO.getReviews()).thenReturn(List.of(record));

        List<ReviewRecord> result = reviewService.getReviews();

        assertEquals(List.of(record), result);
        verify(reviewDAO).getReviews();
    }

    @Test
    void getReviewById_delegatesToDao() throws DatabaseQueryException {
        String reviewId = "r1";
        ReviewRecord record = new ReviewRecord(reviewId, "post-1", "user-1", "Author", 5, "Great", LocalDateTime.now());
        when(reviewDAO.getReviewById(reviewId)).thenReturn(record);

        ReviewRecord result = reviewService.getReviewById(reviewId);

        assertEquals(record, result);
        verify(reviewDAO).getReviewById(reviewId);
    }

    @Test
    void updateReview_delegatesToDao() throws DatabaseQueryException {
        Review review = new Review();

        reviewService.updateReview(review);

        verify(reviewDAO).update(review);
    }

    @Test
    void deleteReview_delegatesToDao() throws DatabaseQueryException {
        String reviewId = "r1";
        String userId = "user-1";

        reviewService.deleteReview(reviewId, userId);

        verify(reviewDAO).delete(reviewId, userId);
    }

    @Test
    void getAverageRating_returnsZero_whenNoReviews() throws DatabaseQueryException {
        String postId = "post-1";
        when(reviewDAO.getReviewsByPostId(postId)).thenReturn(List.of());

        double avg = reviewService.getAverageRating(postId);

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageRating_calculatesMeanOfRatings() throws DatabaseQueryException {
        String postId = "post-1";
        ReviewRecord r1 = new ReviewRecord("r1", postId, "user-1", "A", 4, "Good", LocalDateTime.now());
        ReviewRecord r2 = new ReviewRecord("r2", postId, "user-2", "B", 2, "Ok", LocalDateTime.now());
        when(reviewDAO.getReviewsByPostId(postId)).thenReturn(List.of(r1, r2));

        double avg = reviewService.getAverageRating(postId);

        assertEquals(3.0, avg);
    }

    @Test
    void getAverageRating_treatsNullRatingsAsZero() throws DatabaseQueryException {
        String postId = "post-1";
        ReviewRecord r1 = new ReviewRecord("r1", postId, "user-1", "A", null, "No rating", LocalDateTime.now());
        ReviewRecord r2 = new ReviewRecord("r2", postId, "user-2", "B", 4, "Good", LocalDateTime.now());
        when(reviewDAO.getReviewsByPostId(postId)).thenReturn(List.of(r1, r2));

        double avg = reviewService.getAverageRating(postId);

        assertEquals(2.0, avg);
    }
}

