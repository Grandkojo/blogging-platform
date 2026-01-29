package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.dao.interfaces.TagDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

public class TagService {
    private TagDAO tagDAO;

    public TagService(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    public void createTag(Tag tag) throws DatabaseQueryException, DuplicateResourceException {
        tagDAO.create(tag);
    }

    public List<TagRecord> getAllTags() throws DatabaseQueryException {
        return tagDAO.getAll();
    }

    public TagRecord getTagById(String tagId) throws DatabaseQueryException {
        return tagDAO.getById(tagId);
    }

    public TagRecord getTagByTagName(String tagName) throws DatabaseQueryException {
        return tagDAO.getByTagName(tagName);
    }

    public void linkTagToPost(String postId, String tagId) throws DatabaseQueryException {
        tagDAO.linkTagToPost(postId, tagId);
    }

    public void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException {
        tagDAO.unlinkAllTagsFromPost(postId);
    }

    public List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException {
        return tagDAO.getTagsByPostId(postId);
    }
}
