package com.blogging_platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blogging_platform.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = { "user" })
    Optional<Review> findById(UUID id);

    @EntityGraph(attributePaths = { "user" })
    List<Review> findAll();

    /**
     * Returns all reviews for the given post id.
     */
    @Query("select r from Review r join fetch r.user where r.post.id = :postId")
    List<Review> findByPost_Id(@Param("postId") UUID postId);
}
