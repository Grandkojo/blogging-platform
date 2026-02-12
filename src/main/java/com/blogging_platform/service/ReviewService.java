package com.blogging_platform.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;
import com.blogging_platform.repository.ReviewRepository;

import jakarta.transaction.Transactional;

/**
 * Application service for post reviews (ratings and messages). Delegates to {@link ReviewDAO}
 * and provides average rating calculation.
 */
@Service
@Transactional(rollbackOn = { DatabaseQueryException.class, DuplicateResourceException.class })
public class ReviewService {
    private final ReviewRepository reviewRepository;

    /** Creates a review service with the repository. */
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Creates a new review for a post. One review per user per post.
     *
     * @param review the review (post id, user id, rating, message)
     * @throws DuplicateResourceException if the user has already reviewed the post
     * @throws DatabaseQueryException if the insert fails
     */
    public void createReview(Review review) throws DatabaseQueryException, DuplicateResourceException {
        try {
            reviewRepository.save(review);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Preserve existing behavior where duplicate review (one per user per post) throws DuplicateResourceException
            throw new DuplicateResourceException("User has already reviewed this post");
        }
    }

    /**
     * Returns all reviews for a post.
     *
     * @param postId post id
     * @return list of review records
     * @throws DatabaseQueryException if the query fails
     */
    public List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException {
        UUID postUuid = UUID.fromString(postId);
        return reviewRepository.findByPost_Id(postUuid).stream()
                .map(this::toRecord)
                .toList();
    }

    /**
     * Returns all reviews in the system.
     *
     * @return list of review records
     * @throws DatabaseQueryException if the query fails
     */
    public List<ReviewRecord> getReviews() throws DatabaseQueryException {
        return reviewRepository.findAll().stream()
                .map(this::toRecord)
                .toList();
    }

    /**
     * Fetches a single review by id.
     *
     * @param reviewId review id
     * @return the review record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException {
        UUID id = UUID.fromString(reviewId);
        return reviewRepository.findById(id)
                .map(this::toRecord)
                .orElse(null);
    }

    /**
     * Updates an existing review.
     *
     * @param review the review with updated rating/message
     * @throws DatabaseQueryException if the update fails
     */
    public void updateReview(Review review) throws DatabaseQueryException {
        reviewRepository.save(review);
    }

    /**
     * Deletes a review by id for a given user.
     *
     * @param reviewId review id
     * @param userId   user id requesting the delete (ownership/authorization enforced in DAO)
     * @throws DatabaseQueryException if the delete fails
     */
    public void deleteReview(String reviewId, String userId) throws DatabaseQueryException {
        UUID id = UUID.fromString(reviewId);
        reviewRepository.deleteById(id);
    }

    /**
     * Returns the average rating (1–5) for a post, or 0 if there are no reviews.
     *
     * @param postId post id
     * @return average rating, or 0.0 if no reviews
     * @throws DatabaseQueryException if the query fails
     */
    public double getAverageRating(String postId) throws DatabaseQueryException {
        List<ReviewRecord> reviews = getReviewsByPostId(postId);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (ReviewRecord r : reviews) {
            sum += r.rating() != null ? r.rating() : 0;
        }
        return (double) sum / reviews.size();
    }

    /**
     * Maps a {@link Review} entity to a {@link ReviewRecord} DTO.
     */
    private ReviewRecord toRecord(Review r) {
        if (r == null) {
            return null;
        }
        return new ReviewRecord(
                r.getId() != null ? r.getId().toString() : null,
                r.getPostId() != null ? r.getPostId().toString() : null,
                r.getUserId() != null ? r.getUserId().toString() : null,
                r.getUser() != null ? r.getUser().getName() : null,
                r.getRating(),
                r.getMessage(),
                r.getCreatedAt());
    }
}
