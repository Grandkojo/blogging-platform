package com.blogging_platform.dao.interfaces.implementation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.config.DBConnection;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.User;

public class JdbcUserDAO implements UserDAO {

  @Override
  public void register(User user) {
    String create_user_sql = """
            INSERT INTO users (name, email, role, password) VALUES (?,?,?,?);
        """;
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(create_user_sql)) {
      statement.setString(1, user.getName());
      statement.setString(2, user.getEmail());
      statement.setString(3, user.getRole());
      statement.setString(4, user.getPassword());
      System.out.println(statement.toString());
      int inserted = statement.executeUpdate();
      if (inserted != 1) {
        throw new DatabaseQueryException("Failed to create user: expected 1 row inserted, got " + inserted,
            create_user_sql);
      }

    } catch (SQLException | DatabaseQueryException e) {
        e.printStackTrace();
    }
    // jdbcTemplate.update(create_user_sql, user.getName(), user.getEmail(),
    // user.getRole(), user.getPassword());
  }

  @Override
  public UserRecord login(String email, String password) {
    String sql = """
        SELECT BIN_TO_UUID(id) AS id, name, email, password, role FROM users WHERE email = ?;
        """;
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(1, email);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        if (BCrypt.checkpw(password, rs.getString("password"))) {
          return new UserRecord(rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"));
        }
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void logout() {
      SessionManager.getInstance().logout();
  }

  @Override
  public boolean existsByEmail(String email) {
    String sql = """
        SELECT COUNT(id) AS id FROM users WHERE email = ? LIMIT 1;
        """;
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement statement = conn.prepareStatement(sql)) {

      statement.setString(1, email);

      ResultSet rs = statement.executeQuery();

      if (rs.next()) {
        return rs.getInt("id") > 0;
      }
      return false;

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

}
