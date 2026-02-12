package com.blogging_platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blogging_platform.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    /**
     * Finds a tag by its name.
     */
    Optional<Tag> findByTag(String tag);

    /**
     * Returns all tags linked to the given post id via the post_tags join table.
     */
    List<Tag> findByPosts_Id(UUID postId);
}

