package com.blogging_platform.service;

import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.dao.interfaces.CommentDAO;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

public class CommentService {
    private CommentDAO commentDAO;
    
    public CommentService(CommentDAO commentDAO){
        this.commentDAO = commentDAO;
    }

    public void addComment(Comment comment) throws DatabaseQueryException{
        commentDAO.create(comment);
    }

    public List<CommentRecord> getComments(String postId) throws DatabaseQueryException{
        return commentDAO.getComments(postId);
    }

    public CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException{
        return commentDAO.getComment(commentId);
    }

    public void editComment(Comment comment) throws DatabaseQueryException, CommentNotFoundException{
        commentDAO.edit(comment);
    }

    public void deleteComment(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException{
        commentDAO.delete(commentId, userId);
    }

}
