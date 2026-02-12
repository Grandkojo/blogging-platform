package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;
import com.blogging_platform.repository.ReviewRepository;

class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReview_savesEntity() throws DatabaseQueryException, DuplicateResourceException {
        Review review = new Review();

        reviewService.createReview(review);

        verify(reviewRepository).save(review);
    }

    @Test
    void createReview_wrapsDataIntegrityAsDuplicateResource() {
        Review review = new Review();
        org.springframework.dao.DataIntegrityViolationException ex =
                new org.springframework.dao.DataIntegrityViolationException("duplicate");
        when(reviewRepository.save(review)).thenThrow(ex);

        assertThrows(DuplicateResourceException.class, () -> reviewService.createReview(review));
    }

    @Test
    void getReviewsByPostId_mapsEntitiesToRecords() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Review entity = new Review();
        entity.setId(UUID.randomUUID());
        entity.setPostId(postId);
        entity.setUserId(UUID.randomUUID());
        entity.setRating(5);
        entity.setMessage("Great");
        entity.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findByPost_Id(postId)).thenReturn(List.of(entity));

        List<ReviewRecord> records = reviewService.getReviewsByPostId(postId.toString());

        assertEquals(1, records.size());
        assertEquals(5, records.get(0).rating());
    }

    @Test
    void getAverageRating_returnsZeroWhenNoReviews() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        when(reviewRepository.findByPost_Id(postId)).thenReturn(List.of());

        double avg = reviewService.getAverageRating(postId.toString());

        assertEquals(0.0, avg);
    }

    @Test
    void getAverageRating_calculatesMean() throws DatabaseQueryException {
        UUID postId = UUID.randomUUID();
        Review r1 = new Review();
        r1.setPostId(postId);
        r1.setRating(4);
        Review r2 = new Review();
        r2.setPostId(postId);
        r2.setRating(2);

        when(reviewRepository.findByPost_Id(postId)).thenReturn(List.of(r1, r2));

        double avg = reviewService.getAverageRating(postId.toString());

        assertEquals(3.0, avg);
    }
}

