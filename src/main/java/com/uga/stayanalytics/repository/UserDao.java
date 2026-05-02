package com.uga.stayanalytics.repository;

import com.uga.stayanalytics.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

/** JDBC repository for the {@code users} table. PreparedStatement only. */
@Repository
public class UserDao {

    private final DataSource ds;

    @Autowired
    public UserDao(DataSource ds) { this.ds = ds; }

    /** Q10: Lookup by username (used by the security UserDetailsService). */
    public Optional<User> findByUsername(String username) {
        final String sql =
            "SELECT user_id, username, email, password_hash, role, created_at " +
            "FROM users WHERE username = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByUsername failed", e);
        }
    }

    public boolean usernameTaken(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean emailTaken(String email) {
        final String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("emailTaken failed", e);
        }
    }

    /** Q9: Insert a freshly registered user (BCrypt hash already computed). */
    public long insert(String username, String email, String passwordHash, String role) {
        final String sql =
            "INSERT INTO users (username, email, password_hash, role, created_at) " +
            "VALUES (?, ?, ?, ?, NOW())";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("insert user failed", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
