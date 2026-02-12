package com.blogging_platform.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blogging_platform.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Returns all reviews for the given post id.
     */
    List<Review> findByPost_Id(UUID postId);
}

