package com.blogging_platform.repository;


import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blogging_platform.model.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {


    Optional<Post> findByIdAndUser_Id(UUID id, UUID userId);

}
