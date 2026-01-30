package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

/**
 * Data access interface for comments on posts. Handles create, read, update, and delete.
 */
public interface CommentDAO {

    /**
     * Inserts a new comment on a post.
     *
     * @param comment the comment (content, user id, post id)
     * @throws DatabaseQueryException if the insert fails
     */
    void create(Comment comment) throws DatabaseQueryException;

    /**
     * Returns all comments for a post, ordered by date.
     *
     * @param postId post id
     * @return list of comment records
     * @throws DatabaseQueryException if the query fails
     */
    List<CommentRecord> getComments(String postId) throws DatabaseQueryException;

    /**
     * Fetches a single comment by id.
     *
     * @param commentId comment id
     * @return the comment record
     * @throws CommentNotFoundException if the comment does not exist
     * @throws DatabaseQueryException if the query fails
     */
    CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException;

    /**
     * Updates an existing comment. Only the author may update.
     *
     * @param comment the comment with updated content
     * @throws CommentNotFoundException if the comment does not exist
     * @throws DatabaseQueryException if the update fails
     */
    void edit(Comment comment) throws DatabaseQueryException, CommentNotFoundException;

    /**
     * Deletes a comment. Only the author may delete.
     *
     * @param commentId comment id
     * @param userId    user id (must be the comment author)
     * @throws CommentNotFoundException if the comment does not exist or user is not the author
     * @throws DatabaseQueryException if the delete fails
     */
    void delete(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException;
}
