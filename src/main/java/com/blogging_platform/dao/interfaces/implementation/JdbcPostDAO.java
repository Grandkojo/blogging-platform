package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.model.Post;

public class JdbcPostDAO implements PostDAO {

    @Override
    public void create(Post post) {
        String sql = """
                INSERT INTO posts (user_id, title, content, status, published_datetime) VALUES (UUID_TO_BIN(?),?,?,?,?);
                """;
        try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) {}
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getByID(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByID'");
    }

    @Override
    public List<Post> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    @Override
    public List<Post> search(String keyword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public void edit(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'edit'");
    }

    @Override
    public void delete(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
