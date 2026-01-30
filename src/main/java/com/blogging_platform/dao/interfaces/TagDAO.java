package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

/**
 * Data access interface for tags and post–tag associations. Handles tag CRUD and linking tags to posts.
 */
public interface TagDAO {

    /**
     * Creates a new tag. Tag names must be unique.
     *
     * @param tag the tag (name)
     * @throws DuplicateResourceException if a tag with the same name exists
     * @throws DatabaseQueryException if the insert fails
     */
    void create(Tag tag) throws DatabaseQueryException, DuplicateResourceException;

    /**
     * Returns all tags, ordered by name.
     *
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    List<TagRecord> getAll() throws DatabaseQueryException;

    /**
     * Fetches a tag by id.
     *
     * @param tagId tag id
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    TagRecord getById(String tagId) throws DatabaseQueryException;

    /**
     * Fetches a tag by name.
     *
     * @param tagName tag name
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    TagRecord getByTagName(String tagName) throws DatabaseQueryException;

    /**
     * Links a tag to a post (inserts into post_tags).
     *
     * @param postId post id
     * @param tagId  tag id
     * @throws DatabaseQueryException if the insert fails
     */
    void linkTagToPost(String postId, String tagId) throws DatabaseQueryException;

    /**
     * Removes all tag associations for a post.
     *
     * @param postId post id
     * @throws DatabaseQueryException if the delete fails
     */
    void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException;

    /**
     * Returns all tags linked to a post.
     *
     * @param postId post id
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException;
}
