package com.blogging_platform.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;
import com.blogging_platform.model.Post;
import com.blogging_platform.repository.TagRepository;
import com.blogging_platform.repository.PostRepository;

import jakarta.transaction.Transactional;

/**
 * Application service for tags and post–tag associations. Delegates to {@link TagDAO}.
 */
@Service
@Transactional(rollbackOn = { DatabaseQueryException.class, DuplicateResourceException.class })
public class TagService {
    private final TagRepository tagRepository;
    private final PostRepository postRepository;

    /** Creates a tag service with the repositories. */
    public TagService(TagRepository tagRepository, PostRepository postRepository) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
    }

    /**
     * Creates a new tag. Tag names must be unique.
     *
     * @param tag the tag (name)
     * @throws DuplicateResourceException if a tag with the same name exists
     * @throws DatabaseQueryException if the insert fails
     */
    public void createTag(Tag tag) throws DatabaseQueryException, DuplicateResourceException {
        try {
            tagRepository.save(tag);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Tag already exists: " + tag.getTag());
        }
    }

    /**
     * Returns all tags.
     *
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    public List<TagRecord> getAllTags() throws DatabaseQueryException {
        return tagRepository.findAll().stream()
                .map(t -> new TagRecord(
                        t.getId() != null ? t.getId().toString() : null,
                        t.getTag()))
                .toList();
    }

    /**
     * Fetches a tag by id.
     *
     * @param tagId tag id
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public TagRecord getTagById(String tagId) throws DatabaseQueryException {
        UUID id = UUID.fromString(tagId);
        return tagRepository.findById(id)
                .map(t -> new TagRecord(
                        t.getId() != null ? t.getId().toString() : null,
                        t.getTag()))
                .orElse(null);
    }

    /**
     * Fetches a tag by name.
     *
     * @param tagName tag name
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public TagRecord getTagByTagName(String tagName) throws DatabaseQueryException {
        return tagRepository.findByTag(tagName)
                .map(t -> new TagRecord(
                        t.getId() != null ? t.getId().toString() : null,
                        t.getTag()))
                .orElse(null);
    }

    /**
     * Links a tag to a post.
     *
     * @param postId post id
     * @param tagId  tag id
     * @throws DatabaseQueryException if the insert fails
     */
    public void linkTagToPost(String postId, String tagId) throws DatabaseQueryException {
        UUID postUuid = UUID.fromString(postId);
        UUID tagUuid = UUID.fromString(tagId);
        Post post = postRepository.findById(postUuid).orElseThrow();
        Tag tag = tagRepository.findById(tagUuid).orElseThrow();
        post.getTags().add(tag);
        postRepository.save(post);
    }

    /**
     * Removes all tag associations for a post.
     *
     * @param postId post id
     * @throws DatabaseQueryException if the delete fails
     */
    public void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException {
        UUID postUuid = UUID.fromString(postId);
        Post post = postRepository.findById(postUuid).orElseThrow();
        post.getTags().clear();
        postRepository.save(post);
    }

    /**
     * Returns all tags linked to a post.
     *
     * @param postId post id
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    public List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException {
        UUID postUuid = UUID.fromString(postId);
        return tagRepository.findByPosts_Id(postUuid).stream()
                .map(t -> new TagRecord(
                        t.getId() != null ? t.getId().toString() : null,
                        t.getTag()))
                .toList();
    }
}
