package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

public interface ReviewDAO {
    void create(Review review) throws DatabaseQueryException, DuplicateResourceException;
    List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException;
    ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException;
    void update(Review review) throws DatabaseQueryException;
    void delete(String reviewId) throws DatabaseQueryException;
}
