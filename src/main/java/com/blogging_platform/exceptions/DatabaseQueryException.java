package com.blogging_platform.exceptions;

/**
 * Exception thrown when a database query fails or returns unexpected results.
 */
public class DatabaseQueryException extends DatabaseException {
    
    private final String query;
    
    public DatabaseQueryException(String message) {
        super("DB_QUERY_ERROR", message, "A database error occurred. Please try again.");
        this.query = null;
    }
    
    public DatabaseQueryException(String message, Throwable cause) {
        super("DB_QUERY_ERROR", message, "A database error occurred. Please try again.", cause);
        this.query = null;
    }
    
    public DatabaseQueryException(String message, String query) {
        super("DB_QUERY_ERROR", message, "A database error occurred. Please try again.");
        this.query = query;
    }
    
    public DatabaseQueryException(String message, String query, Throwable cause) {
        super("DB_QUERY_ERROR", message, "A database error occurred. Please try again.", cause);
        this.query = query;
    }
    
    /**
     * Returns the SQL query that caused the exception, if available.
     * 
     * @return the SQL query, or null if not available
     */
    public String getQuery() {
        return query;
    }
}

