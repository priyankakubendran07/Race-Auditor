package com.raceauditor.dao;

import com.raceauditor.database.DatabaseManager;
import com.raceauditor.model.User;
import com.raceauditor.util.PasswordUtil;
import java.sql.*;

public class UserDAO {

    public User findByLogin(String login) throws SQLException {
        String sql = "SELECT id, full_name, email, username, created_at FROM users WHERE username = ? OR email = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public boolean existsByUsernameOrEmail(String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? OR email = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public User create(String fullName, String email, String username, String password) throws SQLException {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash(password, salt);
        String sql = "INSERT INTO users(full_name, email, username, password_hash, salt) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, hash);
            ps.setString(5, salt);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                User u = new User();
                u.setId(rs.getInt(1));
                u.setFullName(fullName);
                u.setEmail(email);
                u.setUsername(username);
                return u;
            }
        }
    }

    public String[] credentialsFor(String login) throws SQLException {
        String sql = "SELECT password_hash, salt FROM users WHERE username = ? OR email = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new String[]{rs.getString(1), rs.getString(2)};
                return null;
            }
        }
    }

    public void updateProfile(int userId, String fullName, String email) throws SQLException {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET full_name=?, email=? WHERE id=?")) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setUsername(rs.getString("username"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}
