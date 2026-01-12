package com.blogging_platform.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for database-related exceptions.
 */
@DisplayName("Database Exception Tests")
class DatabaseExceptionTest {

    @Test
    @DisplayName("DatabaseConnectionException should have correct error code and user message")
    void testDatabaseConnectionException() {
        String message = "Connection failed";
        DatabaseConnectionException exception = new DatabaseConnectionException(message);
        
        assertEquals("DB_CONNECTION_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("connect to the database"));
        assertTrue(exception instanceof DatabaseException);
    }

    @Test
    @DisplayName("DatabaseConnectionException should preserve cause")
    void testDatabaseConnectionExceptionWithCause() {
        String message = "Connection failed";
        Throwable cause = new RuntimeException("Network error");
        DatabaseConnectionException exception = new DatabaseConnectionException(message, cause);
        
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("DatabaseQueryException should have correct error code")
    void testDatabaseQueryException() {
        String message = "Query failed";
        DatabaseQueryException exception = new DatabaseQueryException(message);
        
        assertEquals("DB_QUERY_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("database error"));
    }

    @Test
    @DisplayName("DatabaseQueryException should store query string")
    void testDatabaseQueryExceptionWithQuery() {
        String message = "Query failed";
        String query = "SELECT * FROM users";
        DatabaseQueryException exception = new DatabaseQueryException(message, query);
        
        assertEquals(query, exception.getQuery());
    }

    @Test
    @DisplayName("DatabaseTransactionException should have correct error code")
    void testDatabaseTransactionException() {
        String message = "Transaction failed";
        DatabaseTransactionException exception = new DatabaseTransactionException(message);
        
        assertEquals("DB_TRANSACTION_ERROR", exception.getErrorCode());
        assertTrue(exception.getUserMessage().contains("transaction"));
    }

    @Test
    @DisplayName("DatabaseException should be base class for all database exceptions")
    void testDatabaseExceptionHierarchy() {
        assertTrue(new DatabaseConnectionException("test") instanceof DatabaseException);
        assertTrue(new DatabaseQueryException("test") instanceof DatabaseException);
        assertTrue(new DatabaseTransactionException("test") instanceof DatabaseException);
    }
}

