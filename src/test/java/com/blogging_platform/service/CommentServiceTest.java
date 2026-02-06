package com.blogging_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.dao.interfaces.CommentDAO;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

/**
 * Unit tests for {@link CommentService}.
 *
 * This test class focuses on verifying that the service correctly delegates
 * to {@link CommentDAO} and propagates results and exceptions.
 */
class CommentServiceTest {

    @Mock
    private CommentDAO commentDAO;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commentService = new CommentService(commentDAO);
    }

    @Test
    void addComment_delegatesToDao() throws DatabaseQueryException {
        Comment comment = Comment.forCreate("Nice post", "user-1", "post-1");

        commentService.addComment(comment);

        verify(commentDAO).create(comment);
    }

    @Test
    void addComment_propagatesDatabaseException() throws DatabaseQueryException {
        Comment comment = Comment.forCreate("Nice post", "user-1", "post-1");
        doThrow(new DatabaseQueryException("DB error")).when(commentDAO).create(comment);

        assertThrows(DatabaseQueryException.class, () -> commentService.addComment(comment));
    }

    @Test
    void getCommentsByPostId_returnsListFromDao() throws DatabaseQueryException {
        String postId = "post-1";
        CommentRecord record = new CommentRecord("c1", postId, "user-1", "Author", "Nice", null);
        when(commentDAO.getComments(postId)).thenReturn(List.of(record));

        List<CommentRecord> result = commentService.getComments(postId);

        assertEquals(1, result.size());
        assertEquals(record, result.get(0));
        verify(commentDAO).getComments(postId);
    }

    @Test
    void getAllComments_returnsListFromDao() throws DatabaseQueryException {
        CommentRecord record = new CommentRecord("c1", "post-1", "user-1", "Author", "Nice", null);
        when(commentDAO.getComments()).thenReturn(List.of(record));

        List<CommentRecord> result = commentService.getComments();

        assertEquals(1, result.size());
        assertEquals(record, result.get(0));
        verify(commentDAO).getComments();
    }

    @Test
    void getCommentById_returnsRecordFromDao() throws DatabaseQueryException, CommentNotFoundException {
        String commentId = "c1";
        CommentRecord record = new CommentRecord(commentId, "post-1", "user-1", "Author", "Nice", null);
        when(commentDAO.getComment(commentId)).thenReturn(record);

        CommentRecord result = commentService.getComment(commentId);

        assertEquals(record, result);
        verify(commentDAO).getComment(commentId);
    }

    @Test
    void getCommentById_propagatesNotFound() throws DatabaseQueryException, CommentNotFoundException {
        String commentId = "missing";
        doThrow(new CommentNotFoundException("Not found")).when(commentDAO).getComment(commentId);

        assertThrows(CommentNotFoundException.class, () -> commentService.getComment(commentId));
    }

    @Test
    void editComment_delegatesToDao() throws DatabaseQueryException, CommentNotFoundException {
        Comment comment = Comment.forEdit("c1", "user-1", "Updated");

        commentService.editComment(comment);

        verify(commentDAO).edit(comment);
    }

    @Test
    void editComment_propagatesExceptions() throws DatabaseQueryException, CommentNotFoundException {
        Comment comment = Comment.forEdit("c1", "user-1", "Updated");
        doThrow(new CommentNotFoundException("Not found")).when(commentDAO).edit(comment);

        assertThrows(CommentNotFoundException.class, () -> commentService.editComment(comment));
    }

    @Test
    void deleteComment_delegatesToDao() throws DatabaseQueryException, CommentNotFoundException {
        String commentId = "c1";
        String userId = "user-1";

        commentService.deleteComment(commentId, userId);

        verify(commentDAO).delete(commentId, userId);
    }

    @Test
    void deleteComment_propagatesExceptions() throws DatabaseQueryException, CommentNotFoundException {
        String commentId = "c1";
        String userId = "user-1";
        doThrow(new CommentNotFoundException("Not found")).when(commentDAO).delete(commentId, userId);

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(commentId, userId));
    }
}

