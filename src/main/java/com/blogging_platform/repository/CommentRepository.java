package com.blogging_platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blogging_platform.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    long countByPost_Id(UUID postId);

    List<Comment> findByPost_IdOrderByDatetimeDesc(UUID postId);

    List<Comment> findAllByOrderByDatetimeDesc();

    Optional<Comment> findByIdAndUser_Id(UUID id, UUID userId);
}
