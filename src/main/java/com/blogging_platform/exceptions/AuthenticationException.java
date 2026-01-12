package com.blogging_platform.exceptions;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends BloggingPlatformException {
    
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message, "Invalid email or password. Please try again.");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_ERROR", message, "Invalid email or password. Please try again.", cause);
    }
}

