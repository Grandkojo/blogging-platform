package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.dao.interfaces.CommentDAO;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

/**
 * Application service for comments on posts. Delegates to {@link CommentDAO}.
 */
public class CommentService {
    private CommentDAO commentDAO;

    /** Creates a comment service with the given DAO. */
    public CommentService(CommentDAO commentDAO) {
        this.commentDAO = commentDAO;
    }

    /**
     * Adds a new comment to a post.
     *
     * @param comment the comment (content, user id, post id)
     * @throws DatabaseQueryException if the insert fails
     */
    public void addComment(Comment comment) throws DatabaseQueryException {
        commentDAO.create(comment);
    }

    /**
     * Returns all comments for a post.
     *
     * @param postId post id
     * @return list of comment records
     * @throws DatabaseQueryException if the query fails
     */
    public List<CommentRecord> getComments(String postId) throws DatabaseQueryException {
        return commentDAO.getComments(postId);
    }

    /**
     * Fetches a single comment by id.
     *
     * @param commentId comment id
     * @return the comment record
     * @throws CommentNotFoundException if the comment does not exist
     * @throws DatabaseQueryException if the query fails
     */
    public CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException {
        return commentDAO.getComment(commentId);
    }

    /**
     * Updates an existing comment. Only the author may update.
     *
     * @param comment the comment with updated content
     * @throws CommentNotFoundException if the comment does not exist
     * @throws DatabaseQueryException if the update fails
     */
    public void editComment(Comment comment) throws DatabaseQueryException, CommentNotFoundException {
        commentDAO.edit(comment);
    }

    /**
     * Deletes a comment. Only the author may delete.
     *
     * @param commentId comment id
     * @param userId    user id (must be the comment author)
     * @throws CommentNotFoundException if the comment does not exist or user is not the author
     * @throws DatabaseQueryException if the delete fails
     */
    public void deleteComment(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException {
        commentDAO.delete(commentId, userId);
    }
}
