package com.blogging_platform.exceptions;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BloggingPlatformException {
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, "The requested resource could not be found.");
        this.resourceType = null;
        this.resourceId = null;
    }
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s with ID '%s' not found", resourceType, resourceId),
              String.format("The requested %s could not be found.", resourceType.toLowerCase()));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super("RESOURCE_NOT_FOUND", message, "The requested resource could not be found.", cause);
        this.resourceType = null;
        this.resourceId = null;
    }
    
    /**
     * Returns the type of resource that was not found.
     * 
     * @return the resource type, or null if not specified
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Returns the ID of the resource that was not found.
     * 
     * @return the resource ID, or null if not specified
     */
    public String getResourceId() {
        return resourceId;
    }
}

