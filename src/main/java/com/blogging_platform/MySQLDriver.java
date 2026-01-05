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
            BIN_TO_UUID(p.id) AS id,
            title,
            status,
            published_datetime,
            COALESCE(u.name, 'Unknown') AS author
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE u.id = UUID_TO_BIN(?)
        ORDER BY published_datetime DESC
        """;

    private static String load_posts_by_search = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title,
            p.status,
            p.published_datetime,
            COALESCE(u.name, 'Unknown') AS author
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE LOWER(p.title) LIKE ?
        OR LOWER(u.name) LIKE ?
        ORDER BY p.published_datetime DESC
        """;

    private static String get_post_by_id = """
            SELECT
                BIN_TO_UUID(id) AS id,
                title,
                content,
                status
            FROM posts
            WHERE id = UUID_TO_BIN(?);
            """;

    private static String get_full_post_by_id = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title AS title,
            p.content AS content,
            p.status AS status,
            p.published_datetime AS published_datetime,
            COALESCE(u.name, 'Unknown') AS author
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.id = UUID_TO_BIN(?)
        ORDER BY p.published_datetime DESC
        """;

    private static String update_post = """
            UPDATE posts
            SET title = ?,
                content = ?,
                status = ?,
                published_datetime = ?
            WHERE id = UUID_TO_BIN(?);
            """;

    private static String delete_post = """
            DELETE FROM posts WHERE id = UUID_TO_BIN(?);
            """;
    private static String get_posts = """
            SELECT 
                BIN_TO_UUID(p.id) AS id,
                p.title AS title,
                p.content AS content,
                p.status AS status,
                p.published_datetime AS published_datetime,
                COALESCE(u.name, 'Unknown') AS author
            FROM posts p
            LEFT JOIN users u ON p.user_id = u.id
            WHERE p.status = 'PUBLISHED'
            ORDER BY p.published_datetime DESC
            """;

    private static String get_posts_by_search = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title,
            p.content,
            p.status,
            p.published_datetime,
            COALESCE(u.name, 'Unknown') AS author
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.status = 'PUBLISHED'
        AND (LOWER(p.title) LIKE ?
           OR LOWER(u.name) LIKE ?) OR ? IS NULL
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

    public List<PostRecord> listPosts(String user_id){
        List<PostRecord> posts = new ArrayList<PostRecord>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_posts_sql);
        ) {
            preparedStatement.setString(1, user_id);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                posts.add(new PostRecord(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("status"),
                    rs.getString("author"),
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
                    rs.getString("author"),
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

    public boolean updatePost(String post_id, String title, String content, String status){
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(update_post);
        ) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, status);
            preparedStatement.setObject(4, LocalDateTime.now());
            preparedStatement.setString(5, post_id);
            int inserted = preparedStatement.executeUpdate();
            if (inserted == 1) {
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.out.println("Failed to update post: " + e.getMessage());
        }
        return false;
    }

    public boolean deletePost(String post_id){
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(delete_post);
        ) {
            preparedStatement.setString(1, post_id);
            int deleteed = preparedStatement.executeUpdate();
            if (deleteed == 1) {
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.out.println("Failed to update post: " + e.getMessage());
        }
        return false;
    }

    public ArrayList<String> getPostById(String postId){
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_post_by_id);
        ) {
            preparedStatement.setString(1, postId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                post.add(rs.getString("title"));
                post.add(rs.getString("content"));
                post.add(rs.getString("status"));
            }
            return post;
        } catch (SQLException e) {
            System.out.println("Failed to get post: " + e.getMessage());
        }
        return post;
    }

    public ArrayList<String> getFullPostById(String postId){
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_full_post_by_id);
        ) {
            preparedStatement.setString(1, postId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()){
                post.add(rs.getString("id"));
                post.add(rs.getString("title"));
                post.add(rs.getString("content"));
                post.add(rs.getString("status"));
                post.add(rs.getString("published_datetime"));
                post.add(rs.getString("author"));
            }
            return post;
        } catch (SQLException e) {
            System.out.println("Failed to get post: " + e.getMessage());
        }
        return post;
    }

    public ArrayList<String> getAllPosts(){
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_posts);
        ) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                post.add(rs.getString("title"));
                post.add(rs.getString("content"));
                post.add(rs.getString("status"));
                post.add(rs.getString("author"));
                post.add(rs.getString("published_datetime"));
                post.add(rs.getString("id"));

            }
            return post;
        } catch (SQLException e) {
            System.out.println("Failed to get posts: " + e.getMessage());
        }
        return post;
    }

    public ArrayList<String> getAllPosts(String query){
        String searchTerm = "%" + query.toLowerCase() + "%";
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_posts_by_search);
        ) {
            preparedStatement.setString(1, searchTerm);
            preparedStatement.setString(2, searchTerm);
            preparedStatement.setString(3, searchTerm);


            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                post.add(rs.getString("title"));
                post.add(rs.getString("content"));
                post.add(rs.getString("status"));
                post.add(rs.getString("author"));
                post.add(rs.getString("published_datetime"));
                post.add(rs.getString("id"));

            }
            return post;
        } catch (SQLException e) {
            System.out.println("Failed to get posts: " + e.getMessage());
        }
        return post;
    }
}
