package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for session exceptions.
 */
@DisplayName("Session Exception Tests")
class SessionExceptionTest {

    @Test
    @DisplayName("SessionException should have correct error code")
    void testSessionException() {
        String message = "Session expired";
        SessionException exception = new SessionException(message);
        
        assertEquals("SESSION_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("log in again"));
    }

    @Test
    @DisplayName("SessionException should preserve cause")
    void testSessionExceptionWithCause() {
        String message = "Session error";
        Throwable cause = new RuntimeException("Session timeout");
        SessionException exception = new SessionException(message, cause);
        
        assertEquals(cause, exception.getCause());
    }
}

