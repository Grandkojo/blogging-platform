package com.blogging_platform.exceptions;

/**
 * Exception thrown when a database transaction fails or cannot be completed.
 */
public class DatabaseTransactionException extends DatabaseException {
    
    public DatabaseTransactionException(String message) {
        super("DB_TRANSACTION_ERROR", message, "A database transaction error occurred. The operation could not be completed.");
    }
    
    public DatabaseTransactionException(String message, Throwable cause) {
        super("DB_TRANSACTION_ERROR", message, "A database transaction error occurred. The operation could not be completed.", cause);
    }
}

