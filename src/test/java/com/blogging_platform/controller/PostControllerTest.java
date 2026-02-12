// package com.blogging_platform.controller;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.blogging_platform.ApiResponse;
// import com.blogging_platform.classes.CacheManager;
// import com.blogging_platform.classes.PagedResult;
// import com.blogging_platform.classes.PostRecord;
// import com.blogging_platform.model.Post;
// import com.blogging_platform.service.PostService;
// import com.blogging_platform.service.TagService;

// class PostControllerTest {

//     @Mock
//     private PostService postService;

//     @Mock
//     private CacheManager cacheManager;

//     @Mock
//     private TagService tagService;

//     private PostController controller;

//     private PostRecord samplePost;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         controller = new PostController(postService, cacheManager, tagService);
//         samplePost = new PostRecord(
//             "id",
//             "Title",
//             "Content",
//             "PUBLISHED",
//             "Author",
//             LocalDateTime.now().minusDays(1),
//             LocalDateTime.now(),
//             2,
//             "user-1",
//             List.of("tag1", "tag2")
//         );
//     }

//     @Test
//     void getPosts_rest_usesCacheAndServiceMapper() {
//         PagedResult<PostRecord> page = new PagedResult<>(List.of(samplePost), 0, 10, 1);
//         when(cacheManager.getPaginatedPublishedPosts(0, 10, null, null)).thenReturn(page);
//         when(postService.mapToPostWithTags(page)).thenReturn(page);

//         ResponseEntity<ApiResponse<PagedResult<PostRecord>>> response =
//             controller.getPosts(0, 10, null, null);

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Posts Fetched Successfully", response.getBody().getMessage());
//         assertEquals(1, response.getBody().getData().content().size());
//         verify(cacheManager).getPaginatedPublishedPosts(0, 10, null, null);
//         verify(postService).mapToPostWithTags(page);
//     }

//     @Test
//     void getPostsFull_rest_returnsAllPostsWithTags() {
//         when(postService.getPosts()).thenReturn(List.of(samplePost));
//         when(postService.toPostWithTags(samplePost)).thenReturn(samplePost);

//         ResponseEntity<ApiResponse<Object>> response = controller.getPostsFull();

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Posts Fetched Successfully", response.getBody().getMessage());
//         @SuppressWarnings("unchecked")
//         List<PostRecord> data = (List<PostRecord>) response.getBody().getData();
//         assertEquals(1, data.size());
//         verify(postService).getPosts();
//         verify(postService).toPostWithTags(samplePost);
//     }

//     @Test
//     void getPost_rest_returnsSinglePostWithTags() {
//         when(postService.getPost("id")).thenReturn(samplePost);
//         when(postService.toPostWithTags(samplePost)).thenReturn(samplePost);

//         ResponseEntity<ApiResponse<Object>> response = controller.getPost("id");

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Post Found Successfully", response.getBody().getMessage());
//         assertEquals(samplePost, response.getBody().getData());
//         verify(postService).getPost("id");
//         verify(postService).toPostWithTags(samplePost);
//     }

//     @Test
//     void createPost_rest_callsServiceAndReturnsCreatedId() {
//         Post post = new Post("user-1", "Title", "Content", "PUBLISH");
//         when(postService.createPost(post)).thenReturn("new-id");

//         ResponseEntity<ApiResponse<Object>> response = controller.createPost(post);

//         verify(postService).createPost(post);
//         assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
//         assertEquals("Post Created Successfully", response.getBody().getMessage());
//         assertEquals("new-id", response.getBody().getData());
//     }

//     @Test
//     void editPost_rest_callsServiceAndReturnsUpdated() {
//         Post post = new Post("user-1", "Title", "Content", "PUBLISH");

//         ResponseEntity<ApiResponse<Object>> response = controller.editPost("id", post);

//         verify(postService).updatePost(post, "id");
//         assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
//         assertEquals("Post Updated Successfully", response.getBody().getMessage());
//     }

//     @Test
//     void deletePost_rest_callsServiceAndReturnsAccepted() {
//         ResponseEntity<ApiResponse<Object>> response = controller.deletePost("user-1", "id");

//         verify(postService).deletePost("id", "user-1");
//         assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
//         assertEquals("Post Deleted Successfully", response.getBody().getMessage());
//     }

//     @Test
//     void getPosts_graphql_usesCacheAndMapper() {
//         PagedResult<PostRecord> page = new PagedResult<>(List.of(samplePost), 0, 10, 1);
//         when(cacheManager.getPaginatedPublishedPosts(0, 10, "query", "date_desc"))
//             .thenReturn(page);
//         when(postService.mapToPostWithTags(page)).thenReturn(page);

//         List<PostRecord> result = controller.getPosts("query", 0, 10, "date_desc");

//         assertEquals(1, result.size());
//         verify(cacheManager).getPaginatedPublishedPosts(0, 10, "query", "date_desc");
//         verify(postService).mapToPostWithTags(page);
//     }

//     @Test
//     void getPost_graphql_returnsPostWithTags() {
//         when(postService.getPost("id")).thenReturn(samplePost);
//         when(postService.toPostWithTags(samplePost)).thenReturn(samplePost);

//         PostRecord result = controller.getPostById("id");

//         assertEquals(samplePost, result);
//         verify(postService).getPost("id");
//         verify(postService).toPostWithTags(samplePost);
//     }
// }

