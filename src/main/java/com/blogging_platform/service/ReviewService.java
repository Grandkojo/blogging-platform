package com.blogging_platform.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.exceptions.AuthorizationException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.exceptions.ReviewNotFoundException;
import com.blogging_platform.exceptions.ValidationException;
import com.blogging_platform.model.Review;
import com.blogging_platform.repository.ReviewRepository;

import jakarta.transaction.Transactional;

/**
 * Application service for post reviews (ratings and messages). Delegates to
 * {@link ReviewDAO}
 * and provides average rating calculation.
 */
@Service
@Transactional(rollbackOn = { com.blogging_platform.exceptions.DatabaseQueryException.class,
        com.blogging_platform.exceptions.DuplicateResourceException.class })
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
     * @throws DatabaseQueryException     if the insert fails
     */
    public void createReview(Review review) throws DatabaseQueryException, DuplicateResourceException {
        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            // Preserve existing behavior where duplicate review (one per user per post)
            // throws DuplicateResourceException
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
     * Updates an existing review. Only the author may update.
     *
     * @param review the review with id, userId, and updated rating/message
     * @throws ReviewNotFoundException if the review does not exist
     * @throws AuthorizationException  if the user is not the author of the review
     * @throws ValidationException     if postId in body does not match the review's
     *                                 post
     * @throws DatabaseQueryException  if the update fails
     */
    public void updateReview(Review review)
            throws DatabaseQueryException, ReviewNotFoundException, AuthorizationException, ValidationException {
        if (review.getId() == null) {
            throw new ValidationException("Review id is required");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("User id is required");
        }
        Review existing = reviewRepository.findById(review.getId())
                .orElseThrow(
                        () -> new ReviewNotFoundException("Review with id '" + review.getId() + "' not found.", null));
        if (!existing.getUserId().equals(review.getUserId())) {
            throw new AuthorizationException("User is not the author of this review.");
        }
        if (review.getPostId() != null && !review.getPostId().equals(existing.getPostId())) {
            throw new ValidationException("Post does not match this review.");
        }
        existing.setRating(review.getRating());
        existing.setMessage(review.getMessage());
        reviewRepository.save(existing);
    }

    /**
     * Deletes a review by id for a given user.
     *
     * <p>
     * Only the author of the review may delete it; admins are expected to be
     * enforced at a higher layer (e.g. method security).
     * </p>
     *
     * @param reviewId review id
     * @param userId   user id requesting the delete
     * @throws DatabaseQueryException if the delete fails
     */
    public void deleteReview(String reviewId, String userId) throws DatabaseQueryException {
        UUID id = UUID.fromString(reviewId);
        UUID userUuid = UUID.fromString(userId);
        reviewRepository.findById(id)
                .filter(r -> userUuid.equals(r.getUserId()))
                .ifPresent(reviewRepository::delete);
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
