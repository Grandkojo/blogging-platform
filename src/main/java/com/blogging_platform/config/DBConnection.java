package com.blogging_platform.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Provides JDBC connections using the Spring-configured {@link DataSource}.
 * The DataSource is set at application startup from {@code application.properties}
 * (see {@link DatabaseConfig}).
 */
public class DBConnection {

    private static volatile DataSource dataSource;

    /**
     * Initializes the connection helper with the Spring-managed DataSource.
     * Called once at startup by {@link DatabaseConfig}.
     */
    public static void init(DataSource ds) {
        dataSource = ds;
    }

    /**
     * Returns a new connection from the configured DataSource.
     *
     * @return a JDBC connection (caller must close it)
     * @throws SQLException if the connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        DataSource ds = dataSource;
        if (ds == null) {
            throw new SQLException("Database not initialized: DataSource not set. Ensure DatabaseConfig runs at startup.");
        }
        return ds.getConnection();
    }
}
