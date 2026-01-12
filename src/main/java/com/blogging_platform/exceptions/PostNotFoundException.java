package com.blogging_platform.exceptions;

/**
 * Exception thrown when a post is not found.
 */
public class PostNotFoundException extends ResourceNotFoundException {
    
    public PostNotFoundException(String postId) {
        super("Post", postId);
    }
    
    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

