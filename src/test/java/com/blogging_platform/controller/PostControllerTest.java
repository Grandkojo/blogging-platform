package com.blogging_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blogging_platform.ApiResponse;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.model.Post;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.TagService;

class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostController controller;

    private PostRecord samplePost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        samplePost = new PostRecord(
            UUID.randomUUID().toString(),
            "Title",
            "Content",
            "PUBLISHED",
            "Author",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now(),
            2,
            UUID.randomUUID().toString(),
            List.of("tag1", "tag2")
        );
    }

    @Test
    void getPosts_rest_usesServiceAndMaps() {
        Pageable pageable = PageRequest.of(0, 10);
        when(postService.getPosts(any(Pageable.class))).thenReturn(List.of(samplePost));

        ResponseEntity<ApiResponse<List<PostRecord>>> response =
            controller.getPosts(0, 10, null, "createdAt", "DESC");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals("Posts Fetched Successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(postService).getPosts(any(Pageable.class));
    }

    @Test
    void getPostsFull_rest_returnsAllPosts() {
        when(postService.getPosts()).thenReturn(List.of(samplePost));

        ResponseEntity<ApiResponse<List<PostRecord>>> response = controller.getPostsFull();

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getPost_rest_returnsSinglePost() {
        when(postService.getPost("id")).thenReturn(samplePost);
        when(postService.toPostWithTags(samplePost)).thenReturn(samplePost);

        ResponseEntity<ApiResponse<Object>> response = controller.getPost("id");

        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals(samplePost, response.getBody().getData());
    }

    @Test
    void createPost_rest_callsService() {
        Post post = new Post();

        ResponseEntity<ApiResponse<Object>> response = controller.createPost(post);

        verify(postService).createPost(post);
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
    }

    @Test
    void editPost_rest_callsService() {
        Post post = new Post();

        ResponseEntity<ApiResponse<Object>> response = controller.editPost("id", post);

        verify(postService).updatePost(post, "id");
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
    }

    @Test
    void deletePost_rest_callsService() {
        ResponseEntity<ApiResponse<Object>> response = controller.deletePost("user-1", "id");

        verify(postService).deletePost("id", "user-1");
        assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
    }
}

