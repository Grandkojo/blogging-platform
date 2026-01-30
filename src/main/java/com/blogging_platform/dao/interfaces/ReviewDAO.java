package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

/**
 * Data access interface for post reviews (ratings and messages). Handles create, read, update, and delete.
 */
public interface ReviewDAO {

    /**
     * Inserts a new review for a post. One review per user per post.
     *
     * @param review the review (post id, user id, rating, message)
     * @throws DuplicateResourceException if the user has already reviewed the post
     * @throws DatabaseQueryException if the insert fails
     */
    void create(Review review) throws DatabaseQueryException, DuplicateResourceException;

    /**
     * Returns all reviews for a post.
     *
     * @param postId post id
     * @return list of review records (with author name when available)
     * @throws DatabaseQueryException if the query fails
     */
    List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException;

    /**
     * Fetches a single review by id.
     *
     * @param reviewId review id
     * @return the review record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException;

    /**
     * Updates an existing review.
     *
     * @param review the review with updated rating/message
     * @throws DatabaseQueryException if the update fails
     */
    void update(Review review) throws DatabaseQueryException;

    /**
     * Deletes a review by id.
     *
     * @param reviewId review id
     * @throws DatabaseQueryException if the delete fails
     */
    void delete(String reviewId) throws DatabaseQueryException;
}
