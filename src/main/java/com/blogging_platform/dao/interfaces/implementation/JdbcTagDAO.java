package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.TagDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.model.Tag;

/**
 * JDBC implementation of {@link TagDAO}. Manages tags and post_tags table for linking tags to posts.
 */
public class JdbcTagDAO implements TagDAO {

    @Override
    public void create(Tag tag) throws DatabaseQueryException, DuplicateResourceException {
        String sql = """
                INSERT INTO tags (tag) VALUES (?);
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, tag.getTag());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry error code
                throw new DuplicateResourceException("Tag already exists: " + tag.getTag());
            }
            throw new DatabaseQueryException("Failed to create tag", sql, e);
        }
    }

    @Override
    public List<TagRecord> getAll() throws DatabaseQueryException {
        List<TagRecord> tags = new ArrayList<>();
        String sql = """
                SELECT 
                    BIN_TO_UUID(id) AS id,
                    tag
                FROM tags
                ORDER BY tag ASC
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tags.add(new TagRecord(
                            rs.getString("id"),
                            rs.getString("tag")));
                }
            }
            return tags;
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get all tags", sql, e);
        }
    }

    @Override
    public TagRecord getById(String tagId) throws DatabaseQueryException {
        String sql = """
                SELECT 
                    BIN_TO_UUID(id) AS id,
                    tag
                FROM tags
                WHERE id = UUID_TO_BIN(?)
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, tagId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new TagRecord(
                            rs.getString("id"),
                            rs.getString("tag"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get tag by id", sql, e);
        }
    }

    @Override
    public TagRecord getByTagName(String tagName) throws DatabaseQueryException {
        String sql = """
                SELECT 
                    BIN_TO_UUID(id) AS id,
                    tag
                FROM tags
                WHERE tag = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, tagName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new TagRecord(
                            rs.getString("id"),
                            rs.getString("tag"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get tag by name", sql, e);
        }
    }

    @Override
    public void linkTagToPost(String postId, String tagId) throws DatabaseQueryException {
        String sql = """
                INSERT INTO post_tags (post_id, tag_id) 
                VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?))
                ON DUPLICATE KEY UPDATE post_id = post_id
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            statement.setString(2, tagId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to link tag to post", sql, e);
        }
    }

    @Override
    public void unlinkAllTagsFromPost(String postId) throws DatabaseQueryException {
        String sql = "DELETE FROM post_tags WHERE post_id = UUID_TO_BIN(?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to unlink tags from post", sql, e);
        }
    }

    @Override
    public List<TagRecord> getTagsByPostId(String postId) throws DatabaseQueryException {
        List<TagRecord> tags = new ArrayList<>();
        String sql = """
                SELECT 
                    BIN_TO_UUID(t.id) AS id,
                    t.tag
                FROM tags t
                INNER JOIN post_tags pt ON t.id = pt.tag_id
                WHERE pt.post_id = UUID_TO_BIN(?)
                ORDER BY t.tag ASC
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, postId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tags.add(new TagRecord(
                            rs.getString("id"),
                            rs.getString("tag")));
                }
            }
            return tags;
        } catch (SQLException e) {
            throw new DatabaseQueryException("Failed to get tags by post id", sql, e);
        }
    }
}
