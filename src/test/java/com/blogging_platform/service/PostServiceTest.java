// package com.blogging_platform.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import com.blogging_platform.classes.PostRecord;
// import com.blogging_platform.dao.interfaces.PostDAO;
// import com.blogging_platform.dao.interfaces.UserDAO;
// import com.blogging_platform.exceptions.DatabaseQueryException;
// import com.blogging_platform.exceptions.PostNotFoundException;
// import com.blogging_platform.model.Post;

// /**
//  * Unit tests for {@link PostService}.
//  */
// class PostServiceTest {

//     @Mock
//     private PostDAO postDAO;

//     @Mock
//     private UserDAO userDAO;

//     private PostService postService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         postService = new PostService(postDAO, userDAO);
//     }

//     @Test
//     void createPost_setsPublishFields_whenUserExistsAndStatusPublish() throws DatabaseQueryException {
//         Post post = new Post("user-1", "Title", "Content", "PUBLISH");
//         when(userDAO.existsById("user-1")).thenReturn(true);
//         when(postDAO.create(any(Post.class))).thenReturn("post-1");

//         String id = postService.createPost(post);

//         assertEquals("post-1", id);

//         ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
//         verify(postDAO).create(captor.capture());
//         Post saved = captor.getValue();
//         assertEquals("PUBLISHED", saved.getStatus());
//         assertEquals(true, saved.getIsPublish());
//     }

//     @Test
//     void createPost_setsDraftFields_whenUserExistsAndStatusNotPublish() throws DatabaseQueryException {
//         Post post = new Post("user-1", "Title", "Content", "draft");
//         when(userDAO.existsById("user-1")).thenReturn(true);
//         when(postDAO.create(any(Post.class))).thenReturn("post-1");

//         postService.createPost(post);

//         ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
//         verify(postDAO).create(captor.capture());
//         Post saved = captor.getValue();
//         assertEquals("DRAFT", saved.getStatus());
//         assertEquals(false, saved.getIsPublish());
//     }

//     @Test
//     void createPost_doesNotChangeStatus_whenUserDoesNotExist() throws DatabaseQueryException {
//         Post post = new Post("user-1", "Title", "Content", "UNKNOWN");
//         when(userDAO.existsById("user-1")).thenReturn(false);
//         when(postDAO.create(any(Post.class))).thenReturn("post-1");

//         postService.createPost(post);

//         ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
//         verify(postDAO).create(captor.capture());
//         Post saved = captor.getValue();
//         // status and publish flag should remain as originally set
//         assertEquals("UNKNOWN", saved.getStatus());
//     }

//     @Test
//     void getPostWithUser_delegatesToDao() throws DatabaseQueryException, PostNotFoundException {
//         String postId = "post-1";
//         String userId = "user-1";
//         PostRecord record = new PostRecord(postId, "Title", "Content", "PUBLISHED", "Author", null, null, userId);
//         when(postDAO.getByID(postId, userId)).thenReturn(record);

//         PostRecord result = postService.getPost(postId, userId);

//         assertEquals(record, result);
//         verify(postDAO).getByID(postId, userId);
//     }

//     @Test
//     void getPostById_delegatesToDao() throws DatabaseQueryException, PostNotFoundException {
//         String postId = "post-1";
//         PostRecord record = new PostRecord(postId, "Title", "Content", "PUBLISHED", "Author", null, null);
//         when(postDAO.getByID(postId)).thenReturn(record);

//         PostRecord result = postService.getPost(postId);

//         assertEquals(record, result);
//         verify(postDAO).getByID(postId);
//     }

//     @Test
//     void existsById_delegatesToDao() {
//         String postId = "post-1";
//         when(postDAO.existsById(postId)).thenReturn(true);

//         boolean result = postService.existsById(postId);

//         assertEquals(true, result);
//         verify(postDAO).existsById(postId);
//     }

//     @Test
//     void getPosts_delegatesToDao() throws DatabaseQueryException {
//         List<PostRecord> records = List.of(
//             new PostRecord("post-1", "Title", "Content", "PUBLISHED", "Author", null, null)
//         );
//         when(postDAO.getAll()).thenReturn(records);

//         List<PostRecord> result = postService.getPosts();

//         assertEquals(records, result);
//         verify(postDAO).getAll();
//     }

//     @Test
//     void updatePost_updatesWhenPostAndUserExist() throws DatabaseQueryException, PostNotFoundException {
//         String postId = "post-1";
//         Post post = new Post("user-1", "Title", "Content", "PUBLISH");
//         when(postDAO.existsById(postId)).thenReturn(true);
//         when(userDAO.existsById("user-1")).thenReturn(true);

//         postService.updatePost(post, postId);

//         ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
//         verify(postDAO).edit(captor.capture());
//         Post saved = captor.getValue();
//         assertEquals(postId, saved.getId());
//         assertEquals("PUBLISHED", saved.getStatus());
//         assertEquals(true, saved.getIsPublish());
//     }

//     @Test
//     void updatePost_throwsWhenPostOrUserDoesNotExist() throws DatabaseQueryException {
//         String postId = "post-1";
//         Post post = new Post("user-1", "Title", "Content", "PUBLISH");
//         when(postDAO.existsById(postId)).thenReturn(false);
//         when(userDAO.existsById("user-1")).thenReturn(true);

//         assertThrows(PostNotFoundException.class, () -> postService.updatePost(post, postId));

//         verify(postDAO, never()).edit(any(Post.class));
//     }

//     @Test
//     void deletePost_deletesWhenPostAndUserExist() throws DatabaseQueryException, PostNotFoundException {
//         String postId = "post-1";
//         String userId = "user-1";
//         when(postDAO.existsById(postId)).thenReturn(true);
//         when(userDAO.existsById(userId)).thenReturn(true);

//         postService.deletePost(postId, userId);

//         verify(postDAO).delete(postId, userId);
//     }

//     @Test
//     void deletePost_throwsWhenPostOrUserDoesNotExist() throws DatabaseQueryException {
//         String postId = "post-1";
//         String userId = "user-1";
//         when(postDAO.existsById(postId)).thenReturn(false);
//         when(userDAO.existsById(userId)).thenReturn(true);

//         assertThrows(PostNotFoundException.class, () -> postService.deletePost(postId, userId));

//         verify(postDAO, never()).delete(postId, userId);
//     }
// }

