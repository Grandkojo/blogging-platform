package com.blogging_platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.blogging_platform.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @EntityGraph(attributePaths = { "user" })
    Optional<Comment> findById(UUID id);

    @EntityGraph(attributePaths = { "user" })
    Page<Comment> findAll(Pageable pageable);

    long countByPost_Id(UUID postId);

    @Query("select c.post.id, count(c) from Comment c where c.post.id in :postIds group by c.post.id")
    List<Object[]> countByPostIds(@Param("postIds") List<UUID> postIds);

    @Query("select c from Comment c join fetch c.user where c.post.id = :postId order by c.datetime desc")
    List<Comment> findByPost_IdOrderByDatetimeDesc(@Param("postId") UUID postId);

    @Query("select c from Comment c join fetch c.user order by c.datetime desc")
    List<Comment> findAllByOrderByDatetimeDesc();

    Optional<Comment> findByIdAndUser_Id(UUID id, UUID userId);
}
