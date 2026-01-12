package com.blogging_platform.exceptions;

/**
 * Base exception for all database-related errors.
 */
public class DatabaseException extends BloggingPlatformException {
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(String errorCode, String message, String userMessage) {
        super(errorCode, message, userMessage);
    }
    
    public DatabaseException(String errorCode, String message, String userMessage, Throwable cause) {
        super(errorCode, message, userMessage, cause);
    }
}

