package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.dao.interfaces.TagDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

/**
 * Unit tests for {@link TagService}.
 */
class TagServiceTest {

    @Mock
    private TagDAO tagDAO;

    private TagService tagService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tagService = new TagService(tagDAO);
    }

    @Test
    void createTag_delegatesToDao() throws DatabaseQueryException, DuplicateResourceException {
        Tag tag = new Tag();

        tagService.createTag(tag);

        verify(tagDAO).create(tag);
    }

    @Test
    void getAllTags_delegatesToDao() throws DatabaseQueryException {
        List<TagRecord> records = List.of(new TagRecord("t1", "Tech"));
        when(tagDAO.getAll()).thenReturn(records);

        List<TagRecord> result = tagService.getAllTags();

        assertEquals(records, result);
        verify(tagDAO).getAll();
    }

    @Test
    void getTagById_delegatesToDao() throws DatabaseQueryException {
        String tagId = "t1";
        TagRecord record = new TagRecord(tagId, "Tech");
        when(tagDAO.getById(tagId)).thenReturn(record);

        TagRecord result = tagService.getTagById(tagId);

        assertEquals(record, result);
        verify(tagDAO).getById(tagId);
    }

    @Test
    void getTagByTagName_delegatesToDao() throws DatabaseQueryException {
        String tagName = "Tech";
        TagRecord record = new TagRecord("t1", tagName);
        when(tagDAO.getByTagName(tagName)).thenReturn(record);

        TagRecord result = tagService.getTagByTagName(tagName);

        assertEquals(record, result);
        verify(tagDAO).getByTagName(tagName);
    }

    @Test
    void linkTagToPost_delegatesToDao() throws DatabaseQueryException {
        String postId = "post-1";
        String tagId = "t1";

        tagService.linkTagToPost(postId, tagId);

        verify(tagDAO).linkTagToPost(postId, tagId);
    }

    @Test
    void unlinkAllTagsFromPost_delegatesToDao() throws DatabaseQueryException {
        String postId = "post-1";

        tagService.unlinkAllTagsFromPost(postId);

        verify(tagDAO).unlinkAllTagsFromPost(postId);
    }

    @Test
    void getTagsByPostId_delegatesToDao() throws DatabaseQueryException {
        String postId = "post-1";
        List<TagRecord> records = List.of(new TagRecord("t1", "Tech"));
        when(tagDAO.getTagsByPostId(postId)).thenReturn(records);

        List<TagRecord> result = tagService.getTagsByPostId(postId);

        assertEquals(records, result);
        verify(tagDAO).getTagsByPostId(postId);
    }
}

