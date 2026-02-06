package com.blogging_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.model.Tag;
import com.blogging_platform.service.TagService;

class TagControllerTest {

    @Mock
    private TagService tagService;

    private TagController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TagController(tagService);
    }

    @Test
    void getTags_rest_returnsAllTags() {
        List<TagRecord> records = List.of(new TagRecord("t1", "Tech"));
        when(tagService.getAllTags()).thenReturn(records);

        ResponseEntity<ApiResponse<Object>> response = controller.getTags();

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Tags Fetched Successfully", response.getBody().getMessage());
        assertEquals(records, response.getBody().getData());
        verify(tagService).getAllTags();
    }

    @Test
    void getTag_rest_returnsSingleTag() {
        TagRecord record = new TagRecord("t1", "Tech");
        when(tagService.getTagById("t1")).thenReturn(record);

        ResponseEntity<ApiResponse<Object>> response = controller.getTag("t1");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Tag Found Successfully", response.getBody().getMessage());
        assertEquals(record, response.getBody().getData());
        verify(tagService).getTagById("t1");
    }

    @Test
    void getTagByName_rest_usesService() {
        TagRecord record = new TagRecord("t1", "Tech");
        when(tagService.getTagByTagName("Tech")).thenReturn(record);

        ResponseEntity<ApiResponse<Object>> response = controller.getTagByName("Tech");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Tag Found Successfully", response.getBody().getMessage());
        assertEquals(record, response.getBody().getData());
        verify(tagService).getTagByTagName("Tech");
    }

    @Test
    void getPostTags_rest_returnsTagsForPost() {
        List<TagRecord> records = List.of(new TagRecord("t1", "Tech"));
        when(tagService.getTagsByPostId("p1")).thenReturn(records);

        ResponseEntity<ApiResponse<Object>> response = controller.getPostTags("p1");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Post Tags Found Successfully", response.getBody().getMessage());
        assertEquals(records, response.getBody().getData());
        verify(tagService).getTagsByPostId("p1");
    }

    @Test
    void createTag_rest_callsServiceAndReturnsCreated() {
        Tag tag = new Tag("Tech");

        ResponseEntity<ApiResponse<Object>> response = controller.createPost(tag);

        verify(tagService).createTag(tag);
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
        assertEquals("Tag Created Successfully", response.getBody().getMessage());
    }

    @Test
    void linkTagToPost_rest_callsServiceAndReturnsAccepted() {
        ResponseEntity<ApiResponse<Object>> response = controller.linkTagToPost("t1", "p1");

        verify(tagService).linkTagToPost("p1", "t1");
        assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
        assertEquals("Tag Linked to Post Successfully", response.getBody().getMessage());
    }

    @Test
    void unlinkTagsFromPost_rest_callsServiceAndReturnsAccepted() {
        ResponseEntity<ApiResponse<Object>> response = controller.unlinkTagsFromPost("p1");

        verify(tagService).unlinkAllTagsFromPost("p1");
        assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
        assertEquals("All Tags Unlinked from Post Successfully", response.getBody().getMessage());
    }

    @Test
    void getTags_graphql_delegatesToService() {
        List<TagRecord> records = List.of(new TagRecord("t1", "Tech"));
        when(tagService.getAllTags()).thenReturn(records);

        List<TagRecord> result = controller.getTagss();

        assertEquals(records, result);
        verify(tagService).getAllTags();
    }

    @Test
    void createTag_graphql_callsServiceAndReturnsTrue() {
        Boolean result = controller.createTagMutation("Tech");

        verify(tagService).createTag(org.mockito.Mockito.any(Tag.class));
        assertEquals(true, result);
    }

    @Test
    void linkTagToPost_graphql_callsServiceAndReturnsTrue() {
        Boolean result = controller.linkTagToPostMutation("t1", "p1");

        verify(tagService).linkTagToPost("p1", "t1");
        assertEquals(true, result);
    }

    @Test
    void unlinkTagsFromPost_graphql_callsServiceAndReturnsTrue() {
        Boolean result = controller.unlinkTagsFromPostMutation("p1");

        verify(tagService).unlinkAllTagsFromPost("p1");
        assertEquals(true, result);
    }
}

