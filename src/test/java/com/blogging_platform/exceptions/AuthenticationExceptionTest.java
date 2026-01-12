package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for authentication and authorization exceptions.
 */
@DisplayName("Authentication Exception Tests")
class AuthenticationExceptionTest {

    @Test
    @DisplayName("AuthenticationException should have correct error code and user message")
    void testAuthenticationException() {
        String message = "Invalid credentials";
        AuthenticationException exception = new AuthenticationException(message);
        
        assertEquals("AUTH_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("AuthenticationException should preserve cause")
    void testAuthenticationExceptionWithCause() {
        String message = "Authentication failed";
        Throwable cause = new RuntimeException("Database error");
        AuthenticationException exception = new AuthenticationException(message, cause);
        
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("AuthorizationException should have correct error code")
    void testAuthorizationException() {
        String message = "Not authorized";
        AuthorizationException exception = new AuthorizationException(message);
        
        assertEquals("AUTHORIZATION_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("permission"));
    }

    @Test
    @DisplayName("AuthorizationException should store resource and action")
    void testAuthorizationExceptionWithResourceAndAction() {
        String resource = "post-123";
        String action = "delete";
        AuthorizationException exception = new AuthorizationException(resource, action);
        
        assertEquals(resource, exception.getResource());
        assertEquals(action, exception.getAction());
        assertTrue(exception.getMessage().contains(resource));
        assertTrue(exception.getMessage().contains(action));
    }

    @Test
    @DisplayName("AuthorizationException should handle null resource and action")
    void testAuthorizationExceptionWithNulls() {
        String message = "Not authorized";
        AuthorizationException exception = new AuthorizationException(message);
        
        assertNull(exception.getResource());
        assertNull(exception.getAction());
    }
}

