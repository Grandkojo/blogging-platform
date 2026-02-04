package com.blogging_platform.exceptions;

public class TagNotFoundException extends ResourceNotFoundException {
     public TagNotFoundException(String tagId) {
        super("Tag", tagId);
    }
    
    public TagNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
