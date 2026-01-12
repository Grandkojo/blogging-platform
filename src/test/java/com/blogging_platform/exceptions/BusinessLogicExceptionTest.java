package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for business logic exceptions.
 */
@DisplayName("Business Logic Exception Tests")
class BusinessLogicExceptionTest {

    @Test
    @DisplayName("BusinessLogicException should have correct error code")
    void testBusinessLogicException() {
        String message = "Business rule violated";
        BusinessLogicException exception = new BusinessLogicException(message);
        
        assertEquals("BUSINESS_LOGIC_ERROR", exception.getErrorCode());
        assertEquals(message, exception.getUserMessage());
    }

    @Test
    @DisplayName("BusinessLogicException should preserve cause")
    void testBusinessLogicExceptionWithCause() {
        String message = "Business rule violated";
        Throwable cause = new RuntimeException("Root cause");
        BusinessLogicException exception = new BusinessLogicException(message, cause);
        
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("BusinessLogicException should support custom error code and messages")
    void testBusinessLogicExceptionWithCustomErrorCode() {
        String errorCode = "POST_PUBLISH_ERROR";
        String message = "Cannot publish draft post";
        String userMessage = "You cannot publish this post";
        BusinessLogicException exception = new BusinessLogicException(errorCode, message, userMessage);
        
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(message, exception.getMessage());
        assertEquals(userMessage, exception.getUserMessage());
    }
}

