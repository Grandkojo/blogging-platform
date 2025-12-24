package com.blogging_platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLDriver {

    private static String db_name = Config.get("DB_NAME");
    private static String username = Config.get("USERNAME");
    private static String password = Config.get("PASSWORD");
    private static String databaseUrl = "jdbc:mysql://localhost:3306/" + db_name + "?allowPublicKeyRetrieval=true&useSSL=false";

    private static String create_post_sql = """
            INSERT INTO posts (id, user_id, title, content, status, published_datetime) VALUES (?,?,?,?,?,?);
            """;
    private static String create_user_sql = """
            INSERT INTO users (id, name, email, role) VALUES (?,?,?,?);
            """;

    public void createUser() throws SQLException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(create_user_sql);
        ) {
            
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    public void createPost() throws SQLException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(create_post_sql);
        ) {

            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
