package com.blogging_platform.exceptions;

/**
 * Exception thrown when session-related operations fail.
 */
public class SessionException extends BloggingPlatformException {
    
    public SessionException(String message) {
        super("SESSION_ERROR", message, "Session error. Please log in again.");
    }
    
    public SessionException(String message, Throwable cause) {
        super("SESSION_ERROR", message, "Session error. Please log in again.", cause);
    }
}

