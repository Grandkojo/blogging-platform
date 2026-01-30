package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.ReviewDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Review;

/**
 * JDBC implementation of {@link ReviewDAO}. Persists reviews (rating, message) and joins with users for author names.
 */
public class JdbcReviewDAO implements ReviewDAO {

    @Override
    public void create(Review review) throws DatabaseQueryException, DuplicateResourceException {
        String sql = """
                INSERT INTO reviews (post_id, user_id, rating, message) 
                VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, review.getPostId());
            statement.setString(2, review.getUserId());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getMessage());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error code
                throw new DuplicateResourceException("User has already reviewed this post");
            }
            throw new DatabaseQueryException("Failed to create review", sql, e);
        }
    }

    @Override
    public List<ReviewRecord> getReviewsByPostId(String postId) throws DatabaseQueryException {
        List<ReviewRecord> reviews = new ArrayList<>();
        String sql = """
                SELECT 
                    BIN_TO_UUID(r.id) AS id,
                    BIN_TO_UUID(r.post_id) AS post_id,
                    BIN_TO_UUID(r.user_id) AS user_id,
                    COALESCE(u.name, 'Unknown') AS author_name,
                    r.rating,
                    r.message,
                    r.created_at
                FROM reviews r
                LEFT JOIN users u ON r.user_id = u.id
                WHERE r.post_id = UUID_TO_BIN(?)
                ORDER BY r.created_at DESC
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new ReviewRecord(
                            rs.getString("id"),
                            rs.getString("post_id"),
                            rs.getString("user_id"),
                            rs.getString("author_name"),
                            rs.getInt("rating"),
                            rs.getString("message"),
                            rs.getObject("created_at", LocalDateTime.class)));
                }
            }
            return reviews;
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get reviews by post id", sql, e);
        }
    }

    @Override
    public ReviewRecord getReviewById(String reviewId) throws DatabaseQueryException {
        String sql = """
                SELECT 
                    BIN_TO_UUID(r.id) AS id,
                    BIN_TO_UUID(r.post_id) AS post_id,
                    BIN_TO_UUID(r.user_id) AS user_id,
                    COALESCE(u.name, 'Unknown') AS author_name,
                    r.rating,
                    r.message,
                    r.created_at
                FROM reviews r
                LEFT JOIN users u ON r.user_id = u.id
                WHERE r.id = UUID_TO_BIN(?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, reviewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new ReviewRecord(
                            rs.getString("id"),
                            rs.getString("post_id"),
                            rs.getString("user_id"),
                            rs.getString("author_name"),
                            rs.getInt("rating"),
                            rs.getString("message"),
                            rs.getObject("created_at", LocalDateTime.class));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get review by id", sql, e);
        }
    }

    @Override
    public void update(Review review) throws DatabaseQueryException {
        String sql = """
                UPDATE reviews 
                SET rating = ?, message = ?
                WHERE id = UUID_TO_BIN(?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, review.getRating());
            statement.setString(2, review.getMessage());
            statement.setString(3, review.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to update review", sql, e);
        }
    }

    @Override
    public void delete(String reviewId) throws DatabaseQueryException {
        String sql = """
                DELETE FROM reviews 
                WHERE id = UUID_TO_BIN(?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, reviewId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to delete review", sql, e);
        }
    }
}
