package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;


public class JdbcPostDAO implements PostDAO {

    @Override
    public void create(Post post) throws DatabaseQueryException {
        String sql = """
                INSERT INTO posts (user_id, title, content, status, created_at, published_datetime) VALUES (UUID_TO_BIN(?),?,?,?,?,?);
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, post.getUserId());
            statement.setString(2, post.getTitle());
            statement.setString(3, post.getContent());
            statement.setString(4, post.getStatus());
            statement.setObject(5, LocalDateTime.now());
            if (post.getIsPublish()) {
                statement.setObject(6, LocalDateTime.now());
            } else {
                statement.setObject(6, null);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to create post", sql, e);
        }
    }

    @Override
    public PostRecord getByID(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        String sql = """
                    SELECT
                    BIN_TO_UUID(p.id) AS id,
                    p.title,
                    p.content,
                    p.status,
                    COALESCE(u.name, 'Unknown') AS author,
                    p.created_at,c
                    p.published_datetime,
                    (SELECT COUNT(*) 
                    FROM comments c 
                    WHERE c.post_id = p.id) AS comment_count
                FROM posts p
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.id = UUID_TO_BIN(?) AND p.user_id = UUID_TO_BIN(?);
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            statement.setString(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new PostRecord(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("status"),
                            rs.getString("author"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("published_datetime", LocalDateTime.class),
                            rs.getInt("comment_count"),
                            null);
                } else {
                    throw new PostNotFoundException(postId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to get post", sql, e);
        }
    }

    @Override
    public PostRecord getByID(String postId) throws DatabaseQueryException, PostNotFoundException {
        String sql = """
                    SELECT
                    BIN_TO_UUID(p.id) AS id,
                    BIN_TO_UUID(p.user_id) as user_id,
                    p.title,
                    p.content,
                    p.status,
                    COALESCE(u.name, 'Unknown') AS author,
                    p.created_at,
                    p.published_datetime,
                    (SELECT COUNT(*) 
                    FROM comments c 
                    WHERE c.post_id = p.id) AS comment_count
                FROM posts p
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.id = UUID_TO_BIN(?);
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new PostRecord(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("status"),
                            rs.getString("author"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("published_datetime", LocalDateTime.class),
                            rs.getInt("comment_count"),
                            rs.getString("user_id"));
                } else {
                    throw new PostNotFoundException(postId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to get post", sql, e);
        }
    }

    @Override
    public List<PostRecord> getAll() throws DatabaseQueryException {
        List<PostRecord> posts = new ArrayList<>();
        String sql = """
                SELECT 
                BIN_TO_UUID(p.id) AS id,
                p.title,
                p.content,
                p.status,
                p.published_datetime,
                p.created_at,
                COALESCE(u.name, 'Unknown') AS author,
                (SELECT COUNT(*) 
                FROM comments c 
                WHERE c.post_id = p.id) AS comment_count
            FROM posts p
            LEFT JOIN users u ON p.user_id = u.id
            WHERE p.status = 'PUBLISHED'
            ORDER BY p.published_datetime DESC
        """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    posts.add(new PostRecord(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("status"),
                            rs.getString("author"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("published_datetime", LocalDateTime.class),
                            rs.getInt("comment_count"),
                            null));
                }
            }
            return posts;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to load posts", sql, e);

        }
    }

    @Override
    public List<PostRecord> getAll(String userId) throws DatabaseQueryException {
        List<PostRecord> posts = new ArrayList<>();
        String sql = """
                    SELECT
                    BIN_TO_UUID(p.id) AS id,
                    p.title,
                    p.content,
                    p.status,
                    p.created_at,
                    p.published_datetime,
                    COALESCE(u.name, 'Unknown') AS author,
                    (SELECT COUNT(*) 
                    FROM comments c 
                    WHERE c.post_id = p.id) AS comment_count
                FROM posts p
                LEFT JOIN users u ON p.user_id = u.id
                WHERE p.user_id = UUID_TO_BIN(?)
                ORDER BY p.published_datetime DESC
                        """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    posts.add(new PostRecord(
                            rs.getString("id"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("status"),
                            rs.getString("author"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("published_datetime", LocalDateTime.class),
                            rs.getInt("comment_count"),
                            null));
                }
            }
            return posts;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to load posts", sql, e);

        }
    }

    @Override
    public List<Post> search(String keyword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public void edit(Post post) throws DatabaseQueryException, PostNotFoundException {
        String sql = """
            UPDATE posts
                SET title = ?,
                    content = ?,
                    status = ?,
                    published_datetime = ?
                WHERE id = UUID_TO_BIN(?) AND user_id = UUID_TO_BIN(?);
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) 
        {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getContent());
            statement.setString(3, post.getStatus());

            if (post.getIsPublish()){
                statement.setObject(4, LocalDateTime.now());
            }else {
                statement.setObject(4, null);
            }
            statement.setString(5, post.getId());
            statement.setString(6, post.getUserId());

            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new PostNotFoundException(post.getId());
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to edit post", sql, e);
        }

    }

    @Override
    public void delete(String postId, String userId) throws DatabaseQueryException, PostNotFoundException {
        String sql = """
            DELETE FROM posts WHERE id = UUID_TO_BIN(?) AND user_id = UUID_TO_BIN(?);
        """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) 
        {
            statement.setString(1, postId);
            statement.setString(2, userId);
            int deleted = statement.executeUpdate();
            
            if (deleted == 0) {
                throw new PostNotFoundException(postId);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to delete post", sql, e);
        }
                
    
    }

}
