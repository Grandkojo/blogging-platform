package com.blogging_platform.exceptions;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends ResourceNotFoundException {
    
    public UserNotFoundException(String userId) {
        super("User", userId);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a UserNotFoundException for email lookup.
     * 
     * @param email the email that was not found
     * @return UserNotFoundException with appropriate message
     */
    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException(
            String.format("User with email '%s' not found", email)
        );
    }
}

