package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.dao.interfaces.ReviewDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

/**
 * Application service for post reviews (ratings and messages). Delegates to {@link ReviewDAO}
 * and provides average rating calculation.
 */
public class ReviewService {
    private ReviewDAO reviewDAO;

    /** Creates a review service with the given DAO. */
    public ReviewService(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    /**
     * Creates a new review for a post. One review per user per post.
     *
     * @param review the review (post id, user id, rating, message)
     * @throws DuplicateResourceException if the user has already reviewed the post
     * @throws DatabaseQueryException if the insert fails
     */
    public void createReview(Review review) throws DatabaseQueryException, DuplicateResourceException {
        reviewDAO.create(review);
    }

    /**
     * Returns all reviews for a post.
     *
     * @param postId post id
     * @return list of review records
     * @throws DatabaseQueryException if the query fails
     */
    public List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException {
        return reviewDAO.getReviewsByPostId(postId);
    }

    /**
     * Fetches a single review by id.
     *
     * @param reviewId review id
     * @return the review record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException {
        return reviewDAO.getReviewById(reviewId);
    }

    /**
     * Updates an existing review.
     *
     * @param review the review with updated rating/message
     * @throws DatabaseQueryException if the update fails
     */
    public void updateReview(Review review) throws DatabaseQueryException {
        reviewDAO.update(review);
    }

    /**
     * Deletes a review by id.
     *
     * @param reviewId review id
     * @throws DatabaseQueryException if the delete fails
     */
    public void deleteReview(String reviewId) throws DatabaseQueryException {
        reviewDAO.delete(reviewId);
    }

    /**
     * Returns the average rating (1–5) for a post, or 0 if there are no reviews.
     *
     * @param postId post id
     * @return average rating, or 0.0 if no reviews
     * @throws DatabaseQueryException if the query fails
     */
    public double getAverageRating(String postId) throws DatabaseQueryException {
        List<ReviewRecord> reviews = reviewDAO.getReviewsByPostId(postId);
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (ReviewRecord r : reviews) {
            sum += r.rating() != null ? r.rating() : 0;
        }
        return (double) sum / reviews.size();
    }
}
