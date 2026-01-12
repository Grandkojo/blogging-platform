package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify the exception hierarchy is correct.
 */
@DisplayName("Exception Hierarchy Tests")
class ExceptionHierarchyTest {

    @Test
    @DisplayName("All exceptions should extend BloggingPlatformException")
    void testAllExceptionsExtendBase() {
        assertTrue(new DatabaseException("test") instanceof BloggingPlatformException);
        assertTrue(new ValidationException("test") instanceof BloggingPlatformException);
        assertTrue(new AuthenticationException("test") instanceof BloggingPlatformException);
        assertTrue(new AuthorizationException("test") instanceof BloggingPlatformException);
        assertTrue(new ResourceNotFoundException("test") instanceof BloggingPlatformException);
        assertTrue(new DuplicateResourceException("test") instanceof BloggingPlatformException);
        assertTrue(new BusinessLogicException("test") instanceof BloggingPlatformException);
        assertTrue(new ConfigurationException("test") instanceof BloggingPlatformException);
        assertTrue(new SessionException("test") instanceof BloggingPlatformException);
    }

    @Test
    @DisplayName("Database exceptions should extend DatabaseException")
    void testDatabaseExceptionHierarchy() {
        assertTrue(new DatabaseConnectionException("test") instanceof DatabaseException);
        assertTrue(new DatabaseQueryException("test") instanceof DatabaseException);
        assertTrue(new DatabaseTransactionException("test") instanceof DatabaseException);
    }

    @Test
    @DisplayName("Validation exceptions should extend ValidationException")
    void testValidationExceptionHierarchy() {
        assertTrue(new UserValidationException("test") instanceof ValidationException);
    }

    @Test
    @DisplayName("Resource not found exceptions should extend ResourceNotFoundException")
    void testResourceNotFoundExceptionHierarchy() {
        assertTrue(new PostNotFoundException("test") instanceof ResourceNotFoundException);
        assertTrue(new CommentNotFoundException("test") instanceof ResourceNotFoundException);
        assertTrue(new UserNotFoundException("test") instanceof ResourceNotFoundException);
    }

    @Test
    @DisplayName("Duplicate exceptions should extend DuplicateResourceException")
    void testDuplicateExceptionHierarchy() {
        assertTrue(new DuplicateEmailException("test@example.com") instanceof DuplicateResourceException);
    }
}

