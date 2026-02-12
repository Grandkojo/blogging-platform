// package com.blogging_platform.controller;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.blogging_platform.ApiResponse;
// import com.blogging_platform.classes.CommentRecord;
// import com.blogging_platform.model.Comment;
// import com.blogging_platform.service.CommentService;

// class CommentControllerTest {

//     @Mock
//     private CommentService commentService;

//     private CommentController controller;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//         controller = new CommentController(commentService);
//     }

//     @Test
//     void getComments_rest_returnsAllComments() {
//         List<CommentRecord> records = List.of(
//             new CommentRecord("c1", "p1", "u1", "Author", "Nice", null)
//         );
//         when(commentService.getComments()).thenReturn(records);

//         ResponseEntity<ApiResponse<Object>> response = controller.getComments();

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Comments Fetched Successfully", response.getBody().getMessage());
//         assertEquals(records, response.getBody().getData());
//         verify(commentService).getComments();
//     }

//     @Test
//     void getComment_rest_returnsSingleComment() {
//         CommentRecord record = new CommentRecord("c1", "p1", "u1", "Author", "Nice", null);
//         when(commentService.getComment("c1")).thenReturn(record);

//         ResponseEntity<ApiResponse<Object>> response = controller.getComment("c1");

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Comment Found Successfully", response.getBody().getMessage());
//         assertEquals(record, response.getBody().getData());
//         verify(commentService).getComment("c1");
//     }

//     @Test
//     void getPostComments_rest_returnsCommentsForPost() {
//         List<CommentRecord> records = List.of(
//             new CommentRecord("c1", "p1", "u1", "Author", "Nice", null)
//         );
//         when(commentService.getComments("p1")).thenReturn(records);

//         ResponseEntity<ApiResponse<Object>> response = controller.getPostComments("p1");

//         assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
//         assertEquals("Post Comments Found Successfully", response.getBody().getMessage());
//         assertEquals(records, response.getBody().getData());
//         verify(commentService).getComments("p1");
//     }

//     @Test
//     void getComments_graphql_delegatesToService() {
//         List<CommentRecord> records = List.of(
//             new CommentRecord("c1", "p1", "u1", "Author", "Nice", null)
//         );
//         when(commentService.getComments()).thenReturn(records);

//         List<CommentRecord> result = controller.getCommentss();

//         assertEquals(records, result);
//         verify(commentService).getComments();
//     }

//     @Test
//     void getComment_graphql_delegatesToService() {
//         CommentRecord record = new CommentRecord("c1", "p1", "u1", "Author", "Nice", null);
//         when(commentService.getComment("c1")).thenReturn(record);

//         CommentRecord result = controller.getCommentt("c1");

//         assertEquals(record, result);
//         verify(commentService).getComment("c1");
//     }

//     @Test
//     void getPostComments_graphql_delegatesToService() {
//         List<CommentRecord> records = List.of(
//             new CommentRecord("c1", "p1", "u1", "Author", "Nice", null)
//         );
//         when(commentService.getComments("p1")).thenReturn(records);

//         List<CommentRecord> result = controller.getPostCommentss("p1");

//         assertEquals(records, result);
//         verify(commentService).getComments("p1");
//     }

//     @Test
//     void createComment_rest_callsServiceAndReturnsCreated() {
//         Comment comment = Comment.forCreate("Nice", "u1", "p1");

//         ResponseEntity<ApiResponse<Object>> response = controller.createComment(comment);

//         verify(commentService).addComment(comment);
//         assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
//         assertEquals("Comment Added Successfully", response.getBody().getMessage());
//     }

//     @Test
//     void editComment_rest_callsServiceAndReturnsUpdated() {
//         Comment comment = Comment.forEdit("c1", "u1", "Updated");

//         ResponseEntity<ApiResponse<Object>> response = controller.editPost("c1", comment);

//         verify(commentService).editComment(comment);
//         assertEquals(HttpStatus.CREATED.value(), response.getBody().getStatus());
//         assertEquals("Comment Updated Successfully", response.getBody().getMessage());
//     }

//     @Test
//     void deleteComment_rest_callsServiceAndReturnsAccepted() {
//         ResponseEntity<ApiResponse<Object>> response = controller.deletePost("u1", "c1");

//         verify(commentService).deleteComment("c1", "u1");
//         assertEquals(HttpStatus.ACCEPTED.value(), response.getBody().getStatus());
//         assertEquals("Comment Deleted Successfully", response.getBody().getMessage());
//     }

//     @Test
//     void createComment_graphql_callsServiceAndReturnsTrue() {
//         Boolean result = controller.createCommentMutation("u1", "p1", "Nice");

//         verify(commentService).addComment(org.mockito.Mockito.any(Comment.class));
//         assertEquals(true, result);
//     }

//     @Test
//     void updateComment_graphql_callsServiceAndReturnsTrue() {
//         Boolean result = controller.updateCommentMutation("c1", "u1", "Updated");

//         verify(commentService).editComment(org.mockito.Mockito.any(Comment.class));
//         assertEquals(true, result);
//     }

//     @Test
//     void deleteComment_graphql_callsServiceAndReturnsTrue() {
//         Boolean result = controller.deleteCommentMutation("u1", "c1");

//         verify(commentService).deleteComment("c1", "u1");
//         assertEquals(true, result);
//     }
// }

