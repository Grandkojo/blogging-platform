package com.blogging_platform.exceptions;

/**
 * Exception thrown when database connection fails.
 */
public class DatabaseConnectionException extends DatabaseException {
    
    public DatabaseConnectionException(String message) {
        super("DB_CONNECTION_ERROR", message, "Unable to connect to the database. Please check your connection settings.");
    }
    
    public DatabaseConnectionException(String message, Throwable cause) {
        super("DB_CONNECTION_ERROR", message, "Unable to connect to the database. Please check your connection settings.", cause);
    }
}

