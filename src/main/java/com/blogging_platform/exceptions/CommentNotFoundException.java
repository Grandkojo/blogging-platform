package com.blogging_platform.exceptions;

/**
 * Exception thrown when a comment is not found.
 */
public class CommentNotFoundException extends ResourceNotFoundException {
    
    public CommentNotFoundException(String commentId) {
        super("Comment", commentId);
    }
    
    public CommentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

