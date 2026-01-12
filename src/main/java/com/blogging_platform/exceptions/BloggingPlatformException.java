package com.blogging_platform.exceptions;

/**
 * Base exception class for all blogging platform exceptions.
 * Provides a common structure for all custom exceptions in the system.
 */
public class BloggingPlatformException extends Exception {
    
    private final String errorCode;
    private final String userMessage;
    
    /**
     * Constructs a new BloggingPlatformException with the specified detail message.
     * 
     * @param message the detail message
     */
    public BloggingPlatformException(String message) {
        super(message);
        this.errorCode = null;
        this.userMessage = message;
    }
    
    /**
     * Constructs a new BloggingPlatformException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval)
     */
    public BloggingPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.userMessage = message;
    }
    
    /**
     * Constructs a new BloggingPlatformException with error code, message, and user-friendly message.
     * 
     * @param errorCode the error code for programmatic handling
     * @param message the technical detail message
     * @param userMessage the user-friendly message to display
     */
    public BloggingPlatformException(String errorCode, String message, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    /**
     * Constructs a new BloggingPlatformException with error code, message, user message, and cause.
     * 
     * @param errorCode the error code for programmatic handling
     * @param message the technical detail message
     * @param userMessage the user-friendly message to display
     * @param cause the cause (which is saved for later retrieval)
     */
    public BloggingPlatformException(String errorCode, String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    /**
     * Returns the error code associated with this exception.
     * 
     * @return the error code, or null if not set
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Returns the user-friendly message for this exception.
     * 
     * @return the user message, or the technical message if user message not set
     */
    public String getUserMessage() {
        return userMessage != null ? userMessage : getMessage();
    }
    
    /**
     * Returns a formatted string representation of this exception.
     * 
     * @return formatted string with error code and message
     */
    @Override
    public String toString() {
        if (errorCode != null) {
            return String.format("[%s] %s", errorCode, getMessage());
        }
        return getMessage();
    }
}

