package com.blogging_platform.exceptions;

/**
 * Exception thrown when a business rule is violated.
 */
public class BusinessLogicException extends BloggingPlatformException {
    
    public BusinessLogicException(String message) {
        super("BUSINESS_LOGIC_ERROR", message, message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super("BUSINESS_LOGIC_ERROR", message, message, cause);
    }
    
    public BusinessLogicException(String errorCode, String message, String userMessage) {
        super(errorCode, message, userMessage);
    }
}

