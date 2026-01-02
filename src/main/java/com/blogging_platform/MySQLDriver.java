package com.blogging_platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.User;

import org.mindrot.jbcrypt.BCrypt;


public class MySQLDriver {

    private static String db_name = Config.get("DB_NAME");
    private static String username = Config.get("USERNAME");
    private static String password = Config.get("PASSWORD");
    private static String databaseUrl = "jdbc:mysql://localhost:3306/" + db_name + "?allowPublicKeyRetrieval=true&useSSL=false";

    private static String create_post_sql = """
            INSERT INTO posts (user_id, title, content, status, published_datetime) VALUES (UUID_TO_BIN(?),?,?,?,?);
            """;
    private static String create_user_sql = """
            INSERT INTO users (name, email, role, password) VALUES (?,?,?,?);
            """;

    private static String get_user_sql = """
            SELECT BIN_TO_UUID(id) AS id, name, email, role FROM users WHERE email = ? LIMIT 1;
            """;
    
    private static String find_user_sql = """
            SELECT password FROM users WHERE email = ? LIMIT 1;
        """;

    private static String load_posts_sql = """
        SELECT 
            BIN_TO_UUID(id) AS id,
            title,
            status,
            published_datetime
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        ORDER BY published_datetime DESC
        """;

    private static String load_posts_by_search = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title,
            p.status,
            p.published_datetime,
            u.name AS author_name
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE LOWER(p.title) LIKE ?
        OR LOWER(u.name) LIKE ?
        ORDER BY p.published_datetime DESC
        """;

    public boolean createUser(String name, String email, String role, String ppassword) {
        String hPassword = BCrypt.hashpw(ppassword,BCrypt.gensalt());
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(create_user_sql);
        ) {
            
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, role);
            preparedStatement.setString(4, hPassword);
            int inserted = preparedStatement.executeUpdate();

            if (inserted == 1) {
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    public User getCurrentUser(String email) {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_user_sql);
        ) {
            
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()){
                User user = new User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role")
                );
                return user;
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }



    public boolean validateLogin(String email, String ppassword) {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(find_user_sql);
        ) {
            preparedStatement.setString(1, email.trim());

            try (ResultSet rs = preparedStatement.executeQuery()) {     
                if (rs.next()){
                    String storedHash = rs.getString("password");
                    return BCrypt.checkpw(ppassword, storedHash);
                }
            }
        } catch (SQLException e) {
            System.out.println("Login Error: " + e.getMessage());
        }   
        return false;
    }

    public List<PostRecord> listPosts(){
        List<PostRecord> posts = new ArrayList<PostRecord>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_posts_sql);
        ) {

            ResultSet rs = preparedStatement.executeQuery()   ;
            while (rs.next()) {
                posts.add(new PostRecord(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("status"),
                    rs.getString("name"),
                    rs.getObject("published_datetime", LocalDateTime.class)
                ));
            }
            return posts;

        } catch (SQLException e) {
            System.out.println("Failed to load posts: " + e.getMessage());
        }
        return posts;   
    }

    public List<PostRecord> listPostsBySearch(String query){

        String likeTerm = "%" + query.toLowerCase() + "%";

        List<PostRecord> posts = new ArrayList<PostRecord>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_posts_by_search);
        ) {
            preparedStatement.setString(1, likeTerm);
            preparedStatement.setString(2, likeTerm);

            ResultSet rs = preparedStatement.executeQuery()   ;
            while (rs.next()) {
                posts.add(new PostRecord(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("status"),
                    rs.getString("name"),
                    rs.getObject("published_datetime", LocalDateTime.class)
                ));
            }
            return posts;

        } catch (SQLException e) {
            System.out.println("Failed to load filtered posts: " + e.getMessage());
        }
        return posts;   
    }

    
    
    public boolean createPost(String user_id, String title, String content, String status){
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(create_post_sql);
        ) {
            preparedStatement.setString(1, user_id);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, content);
            preparedStatement.setString(4, status);
            preparedStatement.setObject(5, LocalDateTime.now());

            int inserted = preparedStatement.executeUpdate();

            if (inserted == 1) {
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.out.println("Failed to create post: " + e.getMessage());
        }
        return false;
    }
}
