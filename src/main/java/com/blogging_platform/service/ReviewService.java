package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.dao.interfaces.ReviewDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

public class ReviewService {
    private ReviewDAO reviewDAO;

    public ReviewService(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    public void createReview(Review review) throws DatabaseQueryException, DuplicateResourceException {
        reviewDAO.create(review);
    }

    public List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException {
        return reviewDAO.getReviewsByPostId(postId);
    }

    public ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException {
        return reviewDAO.getReviewById(reviewId);
    }

    public void updateReview(Review review) throws DatabaseQueryException {
        reviewDAO.update(review);
    }

    public void deleteReview(String reviewId) throws DatabaseQueryException {
        reviewDAO.delete(reviewId);
    }

    /** Returns average rating (1-5) for a post, or 0 if no reviews. */
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
