package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

public interface TagDAO {
    void create(Tag tag) throws DatabaseQueryException, DuplicateResourceException;
    List<TagRecord> getAll() throws DatabaseQueryException;
    TagRecord getById(String tagId) throws DatabaseQueryException;
    TagRecord getByTagName(String tagName) throws DatabaseQueryException;
    void linkTagToPost(String postId, String tagId) throws DatabaseQueryException;
    void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException;
    List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException;
}
