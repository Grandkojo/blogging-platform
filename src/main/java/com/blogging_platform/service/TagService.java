package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.dao.interfaces.TagDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

/**
 * Application service for tags and post–tag associations. Delegates to {@link TagDAO}.
 */
public class TagService {
    private TagDAO tagDAO;

    /** Creates a tag service with the given DAO. */
    public TagService(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    /**
     * Creates a new tag. Tag names must be unique.
     *
     * @param tag the tag (name)
     * @throws DuplicateResourceException if a tag with the same name exists
     * @throws DatabaseQueryException if the insert fails
     */
    public void createTag(Tag tag) throws DatabaseQueryException, DuplicateResourceException {
        tagDAO.create(tag);
    }

    /**
     * Returns all tags.
     *
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    public List<TagRecord> getAllTags() throws DatabaseQueryException {
        return tagDAO.getAll();
    }

    /**
     * Fetches a tag by id.
     *
     * @param tagId tag id
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public TagRecord getTagById(String tagId) throws DatabaseQueryException {
        return tagDAO.getById(tagId);
    }

    /**
     * Fetches a tag by name.
     *
     * @param tagName tag name
     * @return the tag record, or null if not found
     * @throws DatabaseQueryException if the query fails
     */
    public TagRecord getTagByTagName(String tagName) throws DatabaseQueryException {
        return tagDAO.getByTagName(tagName);
    }

    /**
     * Links a tag to a post.
     *
     * @param postId post id
     * @param tagId  tag id
     * @throws DatabaseQueryException if the insert fails
     */
    public void linkTagToPost(String postId, String tagId) throws DatabaseQueryException {
        tagDAO.linkTagToPost(postId, tagId);
    }

    /**
     * Removes all tag associations for a post.
     *
     * @param postId post id
     * @throws DatabaseQueryException if the delete fails
     */
    public void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException {
        tagDAO.unlinkAllTagsFromPost(postId);
    }

    /**
     * Returns all tags linked to a post.
     *
     * @param postId post id
     * @return list of tag records
     * @throws DatabaseQueryException if the query fails
     */
    public List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException {
        return tagDAO.getTagsByPostId(postId);
    }
}
