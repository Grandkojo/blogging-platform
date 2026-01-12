package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for validation exceptions.
 */
@DisplayName("Validation Exception Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("ValidationException should store single error message")
    void testValidationExceptionSingleError() {
        String message = "Field is required";
        ValidationException exception = new ValidationException(message);
        
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(1, exception.getValidationErrors().size());
        assertTrue(exception.getValidationErrors().contains(message));
        assertFalse(exception.hasMultipleErrors());
    }

    @Test
    @DisplayName("ValidationException should store multiple error messages")
    void testValidationExceptionMultipleErrors() {
        List<String> errors = Arrays.asList("Name is required", "Email is invalid", "Password too short");
        ValidationException exception = new ValidationException(errors);
        
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(3, exception.getValidationErrors().size());
        assertTrue(exception.hasMultipleErrors());
        assertTrue(exception.getUserMessage().contains("correct the following errors"));
    }

    @Test
    @DisplayName("ValidationException should return immutable copy of errors")
    void testValidationExceptionImmutability() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        ValidationException exception = new ValidationException(errors);
        
        List<String> returnedErrors = exception.getValidationErrors();
        returnedErrors.add("Error 3");
        
        // Original list should not be modified
        assertEquals(2, exception.getValidationErrors().size());
    }

    @Test
    @DisplayName("UserValidationException should create with message")
    void testUserValidationException() {
        String message = "Invalid input";
        UserValidationException exception = new UserValidationException(message);
        
        assertTrue(exception instanceof ValidationException);
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
    }

    @Test
    @DisplayName("UserValidationException should create with field and message")
    void testUserValidationExceptionWithField() {
        String field = "email";
        String message = "Invalid email format";
        UserValidationException exception = new UserValidationException(field, message);
        
        assertTrue(exception.getMessage().contains(field));
        assertTrue(exception.getMessage().contains(message));
    }
}

