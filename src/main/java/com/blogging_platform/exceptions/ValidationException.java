package com.blogging_platform.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when validation fails.
 * Can contain multiple validation errors.
 */
public class ValidationException extends BloggingPlatformException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, message, cause);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(message);
    }
    
    public ValidationException(List<String> errors) {
        super("VALIDATION_ERROR", 
              "Validation failed with " + errors.size() + " error(s)", 
              "Please correct the following errors: " + String.join(", ", errors));
        this.validationErrors = new ArrayList<>(errors);
    }
    
    /**
     * Returns the list of validation errors.
     * 
     * @return list of validation error messages
     */
    public List<String> getValidationErrors() {
        return new ArrayList<>(validationErrors);
    }
    
    /**
     * Returns true if there are multiple validation errors.
     * 
     * @return true if multiple errors exist
     */
    public boolean hasMultipleErrors() {
        return validationErrors.size() > 1;
    }
}

