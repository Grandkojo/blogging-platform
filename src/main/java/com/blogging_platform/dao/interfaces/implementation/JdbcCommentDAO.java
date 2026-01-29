package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.CommentDAO;
import com.blogging_platform.exceptions.CommentNotFoundException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Comment;

public class JdbcCommentDAO implements CommentDAO {

    @Override
    public void create(Comment comment) throws DatabaseQueryException {
        String sql = """
            INSERT INTO comments (user_id, post_id, comment, datetime)
            VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, NOW());                
            """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, comment.getUserId());
            statement.setString(2, comment.getPostId());
            statement.setString(3, comment.getComment());
            int inserted = statement.executeUpdate();
            if (inserted != 1) {
                throw new DatabaseQueryException("Failed to add comment");
            }

        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to add comment post", sql, e);
        }
    }

    @Override
    public List<CommentRecord> getComments(String postId) throws DatabaseQueryException {
        List<CommentRecord> comments = new ArrayList<>();
        String sql = """
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
                ORDER BY c.datetime DESC;           
            """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, postId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    comments.add(new CommentRecord(
                            rs.getString("id"),
                            rs.getString("postId"),
                            rs.getString("userId"),
                            rs.getString("authorName"),
                            rs.getString("comment"),
                            rs.getObject("date", LocalDateTime.class)));       
                }
            }
            return comments;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to add comment post", sql, e);
        }
    }

    @Override
    public void edit(Comment comment) throws DatabaseQueryException, CommentNotFoundException {
        String sql = """
            UPDATE comments 
                SET comment = ?,
                datetime = NOW()
            WHERE id = UUID_TO_BIN(?) AND user_id = UUID_TO_BIN(?)
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) 
        {
            statement.setString(1, comment.getComment());
            statement.setString(2, comment.getId());
            statement.setString(3, comment.getUserId());


            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new CommentNotFoundException(comment.getId());
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to update comment", sql, e);
        }
    }

    @Override
    public void delete(String commentId, String userId) throws DatabaseQueryException, CommentNotFoundException {
        String sql = """
                DELETE FROM comments WHERE id = UUID_TO_BIN(?) AND user_id = UUID_TO_BIN(?);
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql))
        {
            statement.setString(1, commentId);
            statement.setString(2, userId);
            int deleted = statement.executeUpdate();
            
            if (deleted == 0) {
                throw new CommentNotFoundException(commentId);
            }
            if (deleted != 1) {
                throw new DatabaseQueryException("Failed to delete comment");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to delete comment", sql, e);
        }
    }

    @Override
    public CommentRecord getComment(String commentId) throws DatabaseQueryException, CommentNotFoundException {
        String sql = """
                SELECT 
                BIN_TO_UUID(c.id) AS id,
                BIN_TO_UUID(c.post_id) post_id,
                c.comment,
                c.datetime,
                COALESCE(u.name, 'Unknown') AS author_name,
                BIN_TO_UUID(u.id) AS user_id
                FROM comments c
                LEFT JOIN users u ON c.user_id = u.id
                WHERE c.id = UUID_TO_BIN(?);
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql))
        {
            statement.setString(1, commentId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new CommentRecord(
                        rs.getString("id"),
                        rs.getString("post_id"), 
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
            e.printStackTrace();
            throw new DatabaseQueryException("Failed to get comment", sql, e);
        }
    }
    
}
