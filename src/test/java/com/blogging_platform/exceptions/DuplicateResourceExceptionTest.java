package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for duplicate resource exceptions.
 */
@DisplayName("Duplicate Resource Exception Tests")
class DuplicateResourceExceptionTest {

    @Test
    @DisplayName("DuplicateResourceException should have correct error code")
    void testDuplicateResourceException() {
        String message = "Resource already exists";
        DuplicateResourceException exception = new DuplicateResourceException(message);
        
        assertEquals("DUPLICATE_RESOURCE", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("already exists"));
    }

    @Test
    @DisplayName("DuplicateResourceException should store resource type and identifier")
    void testDuplicateResourceExceptionWithTypeAndIdentifier() {
        String resourceType = "User";
        String resourceIdentifier = "test@example.com";
        DuplicateResourceException exception = new DuplicateResourceException(resourceType, resourceIdentifier);
        
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceIdentifier, exception.getResourceIdentifier());
        assertTrue(exception.getMessage().contains(resourceType));
        assertTrue(exception.getMessage().contains(resourceIdentifier));
    }

    @Test
    @DisplayName("DuplicateEmailException should extend DuplicateResourceException")
    void testDuplicateEmailException() {
        String email = "test@example.com";
        DuplicateEmailException exception = new DuplicateEmailException(email);
        
        assertTrue(exception instanceof DuplicateResourceException);
        assertEquals("User", exception.getResourceType());
        assertEquals(email, exception.getResourceIdentifier());
    }

    @Test
    @DisplayName("DuplicateResourceException should handle null values")
    void testDuplicateResourceExceptionWithNulls() {
        String message = "Resource exists";
        DuplicateResourceException exception = new DuplicateResourceException(message);
        
        assertNull(exception.getResourceType());
        assertNull(exception.getResourceIdentifier());
    }
}

