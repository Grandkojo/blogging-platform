package com.blogging_platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.User;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DatabaseConnectionException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.UserNotFoundException;

import org.mindrot.jbcrypt.BCrypt;


public class MySQLDriver {

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
        } catch (com.blogging_platform.exceptions.ConfigurationException e) {
            throw new RuntimeException("Failed to initialize database configuration", e);
        }
    }

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
            BIN_TO_UUID(p.user_id) AS user_id,
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

    private static String delete_comment = """
            DELETE FROM comments WHERE id = UUID_TO_BIN(?);
            """;

    private static String get_posts = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title,
            p.content,
            p.status,
            p.published_datetime,
            COALESCE(u.name, 'Unknown') AS author,
            (SELECT COUNT(*) 
            FROM comments c 
            WHERE c.post_id = p.id) AS comment_count
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.status = 'PUBLISHED'
        ORDER BY p.published_datetime DESC
        """;

        

    // private static String get_posts_by_search = """
    //     SELECT 
    //         BIN_TO_UUID(p.id) AS id,
    //         p.title,
    //         p.content,
    //         p.status,
    //         p.published_datetime,
    //         COALESCE(u.name, 'Unknown') AS author
    //     FROM posts p
    //     LEFT JOIN users u ON p.user_id = u.id
    //     WHERE p.status = 'PUBLISHED'
    //     AND (LOWER(p.title) LIKE ?
    //        OR LOWER(u.name) LIKE ?) OR ? IS NULL
    //     ORDER BY p.published_datetime DESC;
    //     """;

    private static String get_posts_by_search = """
        SELECT 
            BIN_TO_UUID(p.id) AS id,
            p.title,
            p.content,
            p.status,
            p.published_datetime,
            COALESCE(u.name, 'Unknown') AS author,
            (SELECT COUNT(*) 
            FROM comments c 
            WHERE c.post_id = p.id) AS comment_count,
            MATCH(p.title) AGAINST (? IN NATURAL LANGUAGE MODE) AS title_score,
            MATCH(u.name) AGAINST (? IN NATURAL LANGUAGE MODE) AS author_score
        FROM posts p
        LEFT JOIN users u ON p.user_id = u.id
        WHERE p.status = 'PUBLISHED'
        AND (MATCH(p.title) AGAINST (? IN NATURAL LANGUAGE MODE)  > 0 
            OR MATCH(u.name) AGAINST (? IN NATURAL LANGUAGE MODE) > 0)
        ORDER BY (title_score + author_score) DESC
        """;

    private static String load_comments_by_post_id = """
            SELECT 
                BIN_TO_UUID(c.id) AS id,
                BIN_TO_UUID(c.post_id) AS postId,
                BIN_TO_UUID(c.user_id) AS userId,
                COALESCE(u.name, 'Unknown') AS authorName,
                c.comment,
                c.datetime AS date
                FROM comments c
                LEFT JOIN users u ON c.user_id = u.id
                WHERE c.post_id = UUID_TO_BIN(?)
                ORDER BY c.datetime DESC
            """;

    private static String post_comment = """
            INSERT INTO comments (user_id, post_id, comment, datetime)
            VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, NOW());
            """;

    private static String update_comment = """
            UPDATE comments 
            SET comment = ?,
                datetime = NOW()
            WHERE id = UUID_TO_BIN(?)
            """;
    
    private static String get_comment_by_id = """
            SELECT 
                BIN_TO_UUID(c.id) AS id,
                c.comment,
                c.datetime,
                COALESCE(u.name, 'Unknown') AS author_name,
                BIN_TO_UUID(u.id) AS user_id
            FROM comments c
            LEFT JOIN users u ON c.user_id = u.id
            WHERE c.id = UUID_TO_BIN(?)
            """;

    public void createUser(String name, String email, String role, String ppassword) 
            throws DatabaseException, DuplicateEmailException {
        String hPassword = BCrypt.hashpw(ppassword, BCrypt.gensalt());
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(create_user_sql);
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, role);
            preparedStatement.setString(4, hPassword);
            int inserted = preparedStatement.executeUpdate();

            if (inserted != 1) {
                throw new DatabaseQueryException("Failed to create user: expected 1 row inserted, got " + inserted, create_user_sql);
            }

        } catch (SQLException e) {
            // MySQL error code 1062 is duplicate entry
            if (e.getErrorCode() == 1062) {
                throw new DuplicateEmailException(email, e);
            }
            // Check if it's a connection error
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while creating user", e);
            }
            throw new DatabaseQueryException("Failed to create user", create_user_sql, e);
        }
    }

    public User getCurrentUser(String email) throws DatabaseException, UserNotFoundException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_user_sql);
        ) {
            preparedStatement.setString(1, email);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                } else {
                    throw UserNotFoundException.forEmail(email);
                }
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while retrieving user", e);
            }
            throw new DatabaseQueryException("Failed to retrieve user", get_user_sql, e);
        } catch (UserNotFoundException e) {
            throw e; // Re-throw UserNotFoundException
        }
    }



    public boolean validateLogin(String email, String ppassword) throws DatabaseException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(find_user_sql);
        ) {
            preparedStatement.setString(1, email.trim());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return BCrypt.checkpw(ppassword, storedHash);
                }
                return false; // User not found or password doesn't match
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while validating login", e);
            }
            throw new DatabaseQueryException("Failed to validate login", find_user_sql, e);
        }
    }

    public List<PostRecord> listPosts(String user_id) throws DatabaseException {
        List<PostRecord> posts = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_posts_sql);
        ) {
            preparedStatement.setString(1, user_id);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    posts.add(new PostRecord(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("author"),
                        rs.getObject("published_datetime", LocalDateTime.class)
                    ));
                }
            }
            return posts;

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while loading posts", e);
            }
            throw new DatabaseQueryException("Failed to load posts", load_posts_sql, e);
        }
    }

    public List<PostRecord> listPostsBySearch(String query) throws DatabaseException {
        String likeTerm = "%" + query.toLowerCase() + "%";
        List<PostRecord> posts = new ArrayList<>();
        
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_posts_by_search);
        ) {
            preparedStatement.setString(1, likeTerm);
            preparedStatement.setString(2, likeTerm);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    posts.add(new PostRecord(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("author"),
                        rs.getObject("published_datetime", LocalDateTime.class)
                    ));
                }
            }
            return posts;

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while searching posts", e);
            }
            throw new DatabaseQueryException("Failed to search posts", load_posts_by_search, e);
        }
    }

    
    
    public void createPost(String user_id, String title, String content, String status) 
            throws DatabaseException {
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

            if (inserted != 1) {
                throw new DatabaseQueryException("Failed to create post: expected 1 row inserted, got " + inserted, create_post_sql);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while creating post", e);
            }
            throw new DatabaseQueryException("Failed to create post", create_post_sql, e);
        }
    }

    public void updatePost(String post_id, String title, String content, String status) 
            throws DatabaseException, PostNotFoundException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(update_post);
        ) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, status);
            preparedStatement.setObject(4, LocalDateTime.now());
            preparedStatement.setString(5, post_id);
            
            int updated = preparedStatement.executeUpdate();
            if (updated == 0) {
                throw new PostNotFoundException(post_id);
            }
            if (updated != 1) {
                throw new DatabaseQueryException("Failed to update post: expected 1 row updated, got " + updated, update_post);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while updating post", e);
            }
            throw new DatabaseQueryException("Failed to update post", update_post, e);
        } catch (PostNotFoundException e) {
            throw e; 
        }
    }

    public void deletePost(String post_id) throws DatabaseException, PostNotFoundException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(delete_post);
        ) {
            preparedStatement.setString(1, post_id);
            int deleted = preparedStatement.executeUpdate();
            
            if (deleted == 0) {
                throw new PostNotFoundException(post_id);
            }
            if (deleted != 1) {
                throw new DatabaseQueryException("Failed to delete post: expected 1 row deleted, got " + deleted, delete_post);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while deleting post", e);
            }
            throw new DatabaseQueryException("Failed to delete post", delete_post, e);
        } catch (PostNotFoundException e) {
            throw e; 
        }
    }

    public ArrayList<String> getPostById(String postId) throws DatabaseException, PostNotFoundException {
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_post_by_id);
        ) {
            preparedStatement.setString(1, postId);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    post.add(rs.getString("title"));
                    post.add(rs.getString("content"));
                    post.add(rs.getString("status"));
                } else {
                    throw new PostNotFoundException(postId);
                }
            }
            return post;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while retrieving post", e);
            }
            throw new DatabaseQueryException("Failed to get post", get_post_by_id, e);
        } catch (PostNotFoundException e) {
            throw e; 
        }
    }

    public ArrayList<String> getFullPostById(String postId) throws DatabaseException, PostNotFoundException {
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_full_post_by_id);
        ) {
            preparedStatement.setString(1, postId);
            
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    post.add(rs.getString("id"));
                    post.add(rs.getString("title"));
                    post.add(rs.getString("content"));
                    post.add(rs.getString("status"));
                    post.add(rs.getString("published_datetime"));
                    post.add(rs.getString("author"));
                    post.add(rs.getString("user_id"));
                } else {
                    throw new PostNotFoundException(postId);
                }
            }
            return post;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while retrieving post", e);
            }
            throw new DatabaseQueryException("Failed to get post", get_full_post_by_id, e);
        } catch (PostNotFoundException e) {
            throw e; 
        }
    }

    public ArrayList<String> getAllPosts() throws DatabaseException {
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_posts);
        ) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    post.add(rs.getString("title"));
                    post.add(rs.getString("content"));
                    post.add(rs.getString("status"));
                    post.add(rs.getString("author"));
                    post.add(rs.getString("published_datetime"));
                    post.add(rs.getString("id"));
                    post.add(String.valueOf(rs.getInt("comment_count")));
                }
            }
            return post;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while retrieving posts", e);
            }
            throw new DatabaseQueryException("Failed to get posts", get_posts, e);
        }
    }

    public ArrayList<String> getAllPosts(String query) throws DatabaseException {
        String searchTerm = "%" + query.toLowerCase() + "%";
        ArrayList<String> post = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(get_posts_by_search);
        ) {
            preparedStatement.setString(1, searchTerm);
            preparedStatement.setString(2, searchTerm);
            preparedStatement.setString(3, searchTerm);
            preparedStatement.setString(4, searchTerm);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    post.add(rs.getString("title"));
                    post.add(rs.getString("content"));
                    post.add(rs.getString("status"));
                    post.add(rs.getString("author"));
                    post.add(rs.getString("published_datetime"));
                    post.add(rs.getString("id"));
                    post.add(rs.getString("comment_count"));
                }
            }
            return post;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while searching posts", e);
            }
            throw new DatabaseQueryException("Failed to search posts", get_posts_by_search, e);
        }
    }

    public List<CommentRecord> getCommentsByPostId(String postId) throws DatabaseException {
        List<CommentRecord> comments = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(load_comments_by_post_id);
        ) {
            preparedStatement.setString(1, postId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    comments.add(new CommentRecord(
                        rs.getString("id"),
                        rs.getString("postId"),
                        rs.getString("userId"),
                        rs.getString("authorName"),
                        rs.getString("comment"),
                        rs.getObject("date", LocalDateTime.class)
                    ));
                }
            }
            return comments;

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while loading comments", e);
            }
            throw new DatabaseQueryException("Failed to load comments", load_comments_by_post_id, e);
        }
    }

    public void addComment(String postId, String userId, String content) 
            throws DatabaseException {
        try (
            Connection conn = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = conn.prepareStatement(post_comment);
        ) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, postId);
            preparedStatement.setString(3, content);

            int inserted = preparedStatement.executeUpdate();
            if (inserted != 1) {
                throw new DatabaseQueryException("Failed to add comment: expected 1 row inserted, got " + inserted, post_comment);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while adding comment", e);
            }
            throw new DatabaseQueryException("Failed to add comment", post_comment, e);
        }
    }

    public void deleteComment(String commentId) throws DatabaseException, CommentNotFoundException {
        try (
            Connection connection = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(delete_comment);
        ) {
            preparedStatement.setString(1, commentId);
            int deleted = preparedStatement.executeUpdate();
            
            if (deleted == 0) {
                throw new CommentNotFoundException(commentId);
            }
            if (deleted != 1) {
                throw new DatabaseQueryException("Failed to delete comment: expected 1 row deleted, got " + deleted, delete_comment);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while deleting comment", e);
            }
            throw new DatabaseQueryException("Failed to delete comment", delete_comment, e);
        } catch (CommentNotFoundException e) {
            throw e; // Re-throw CommentNotFoundException
        }
    }

    public void updateComment(String commentId, String newContent) 
            throws DatabaseException, CommentNotFoundException {
        try (
            Connection conn = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement pstmt = conn.prepareStatement(update_comment);
        ) {
            pstmt.setString(1, newContent);
            pstmt.setString(2, commentId);

            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                throw new CommentNotFoundException(commentId);
            }
            if (updated != 1) {
                throw new DatabaseQueryException("Failed to update comment: expected 1 row updated, got " + updated, update_comment);
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while updating comment", e);
            }
            throw new DatabaseQueryException("Failed to update comment", update_comment, e);
        } catch (CommentNotFoundException e) {
            throw e; // Re-throw CommentNotFoundException
        }
    }


    public CommentRecord getCommentById(String commentId, String currentPostId) 
            throws DatabaseException, CommentNotFoundException {
        try (
            Connection conn = DriverManager.getConnection(databaseUrl, username, password);
            PreparedStatement pstmt = conn.prepareStatement(get_comment_by_id);
        ) {
            pstmt.setString(1, commentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new CommentRecord(
                        rs.getString("id"),
                        currentPostId, 
                        rs.getString("user_id"),
                        rs.getString("author_name"),
                        rs.getString("comment"),
                        rs.getObject("datetime", LocalDateTime.class)
                    );
                } else {
                    throw new CommentNotFoundException(commentId);
                }
            }
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 0 || e.getMessage().contains("Communications link failure")) {
                throw new DatabaseConnectionException("Failed to connect to database while retrieving comment", e);
            }
            throw new DatabaseQueryException("Failed to get comment", get_comment_by_id, e);
        } catch (CommentNotFoundException e) {
            throw e; // Re-throw CommentNotFoundException
        }
    }

}
