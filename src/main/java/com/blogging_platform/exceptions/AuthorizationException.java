package com.blogging_platform.exceptions;

/**
 * Exception thrown when a user is not authorized to perform an action.
 */
public class AuthorizationException extends BloggingPlatformException {
    
    private final String resource;
    private final String action;
    
    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message, "You do not have permission to perform this action.");
        this.resource = null;
        this.action = null;
    }
    
    public AuthorizationException(String resource, String action) {
        super("AUTHORIZATION_ERROR", 
              String.format("User not authorized to %s on resource: %s", action, resource),
              "You do not have permission to perform this action.");
        this.resource = resource;
        this.action = action;
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super("AUTHORIZATION_ERROR", message, "You do not have permission to perform this action.", cause);
        this.resource = null;
        this.action = null;
    }
    
    /**
     * Returns the resource that the user tried to access.
     * 
     * @return the resource identifier, or null if not specified
     */
    public String getResource() {
        return resource;
    }
    
    /**
     * Returns the action that the user tried to perform.
     * 
     * @return the action, or null if not specified
     */
    public String getAction() {
        return action;
    }
}

