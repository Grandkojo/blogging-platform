package com.blogging_platform.dao.interfaces;

import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

public interface CommentDAO {

    void create(Comment comment) throws DatabaseQueryException;

    List<CommentRecord> getComments(String postId) throws DatabaseQueryException;

    CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException;

    void edit(Comment comment) throws DatabaseQueryException, CommentNotFoundException;

    void delete(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException;
}
