package com.blogging_platform.exceptions;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class DuplicateResourceException extends BloggingPlatformException {
    
    private final String resourceType;
    private final String resourceIdentifier;
    
    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message, "This resource already exists.");
        this.resourceType = null;
        this.resourceIdentifier = null;
    }
    
    public DuplicateResourceException(String resourceType, String resourceIdentifier) {
        super("DUPLICATE_RESOURCE", 
              String.format("%s with identifier '%s' already exists", resourceType, resourceIdentifier),
              String.format("This %s already exists.", resourceType.toLowerCase()));
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super("DUPLICATE_RESOURCE", message, "This resource already exists.", cause);
        this.resourceType = null;
        this.resourceIdentifier = null;
    }
    
    /**
     * Returns the type of resource that is duplicated.
     * 
     * @return the resource type, or null if not specified
     */
    public String getResourceType() {
        return resourceType;
    }
    
    /**
     * Returns the identifier of the duplicated resource.
     * 
     * @return the resource identifier, or null if not specified
     */
    public String getResourceIdentifier() {
        return resourceIdentifier;
    }
}

