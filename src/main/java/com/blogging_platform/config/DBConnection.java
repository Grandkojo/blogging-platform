package com.blogging_platform.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.blogging_platform.exceptions.ConfigurationException;

public class DBConnection {
    private static String db_name;
    private static String username;
    private static String password;
    private static String databaseUrl;
    static {
        try {
            db_name = Config.get("DB_NAME");
            username = Config.get("USERNAME");
            password = Config.get("PASSWORD");
            databaseUrl = "jdbc:mysql://localhost:3306/" + db_name + "?allowPublicKeyRetrieval=true&useSSL=false";
        } catch(ConfigurationException e){
            throw new RuntimeException("Failed to initialize database configuration", e);

        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, username, password);
    }
}

