package com.blogging_platform.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Wires the Spring Boot auto-configured DataSource (from {@code application.properties})
 * into {@link DBConnection} so existing DAOs continue to use {@code DBConnection.getConnection()}.
 */
@Configuration
public class DatabaseConfig {

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initDbConnection() {
        DBConnection.init(dataSource);
    }
}
