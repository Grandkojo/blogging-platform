package com.blogging_platform.exceptions;

/**
 * Exception thrown when attempting to register with an email that already exists.
 */
public class DuplicateEmailException extends DuplicateResourceException {
    
    public DuplicateEmailException(String email) {
        super("User", email);
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

