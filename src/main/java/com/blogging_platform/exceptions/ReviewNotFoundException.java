package com.blogging_platform.exceptions;

public class ReviewNotFoundException extends ResourceNotFoundException {
     public ReviewNotFoundException(String reviewId) {
        super("Review", reviewId);
    }
    
    public ReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
