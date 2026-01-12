package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the base BloggingPlatformException class.
 */
@DisplayName("BloggingPlatformException Tests")
class BloggingPlatformExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void testConstructorWithMessage() {
        String message = "Test error message";
        BloggingPlatformException exception = new BloggingPlatformException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getErrorCode());
        assertEquals(message, exception.getUserMessage());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testConstructorWithMessageAndCause() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");
        BloggingPlatformException exception = new BloggingPlatformException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getErrorCode());
        assertEquals(message, exception.getUserMessage());
    }

    @Test
    @DisplayName("Should create exception with error code, message, and user message")
    void testConstructorWithErrorCode() {
        String errorCode = "TEST_ERROR";
        String message = "Technical error message";
        String userMessage = "User-friendly error message";
        BloggingPlatformException exception = new BloggingPlatformException(errorCode, message, userMessage);
        
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(userMessage, exception.getUserMessage());
    }

    @Test
    @DisplayName("Should create exception with all parameters including cause")
    void testConstructorWithAllParameters() {
        String errorCode = "TEST_ERROR";
        String message = "Technical error message";
        String userMessage = "User-friendly error message";
        Throwable cause = new RuntimeException("Root cause");
        BloggingPlatformException exception = new BloggingPlatformException(errorCode, message, userMessage, cause);
        
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(userMessage, exception.getUserMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("toString should include error code when present")
    void testToStringWithErrorCode() {
        String errorCode = "TEST_ERROR";
        String message = "Test message";
        BloggingPlatformException exception = new BloggingPlatformException(errorCode, message, message);
        
        String result = exception.toString();
        assertTrue(result.contains(errorCode));
        assertTrue(result.contains(message));
    }

    @Test
    @DisplayName("toString should not include error code when absent")
    void testToStringWithoutErrorCode() {
        String message = "Test message";
        BloggingPlatformException exception = new BloggingPlatformException(message);
        
        String result = exception.toString();
        assertEquals(message, result);
    }
}

