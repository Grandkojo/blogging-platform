package com.blogging_platform.exceptions;

/**
 * Exception thrown when user input validation fails.
 */
public class UserValidationException extends ValidationException {
    
    public UserValidationException(String message) {
        super(message);
    }
    
    public UserValidationException(String field, String message) {
        super(String.format("Validation failed for field '%s': %s", field, message));
    }
}

