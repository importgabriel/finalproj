package com.uga.stayanalytics.repository;

import com.uga.stayanalytics.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC repository for the review table. */
@Repository
public class ReviewDao {

    private final DataSource ds;

    @Autowired
    public ReviewDao(DataSource ds) { this.ds = ds; }

    /** Q2: Insert a review (upsert: if user already reviewed, update). */
    public void upsert(long userId, String contentId, int rating, String comment) {
        final String sql =
            "INSERT INTO review (user_id, content_id, rating, comment, created_at) " +
            "VALUES (?, ?, ?, ?, NOW()) " +
            "ON DUPLICATE KEY UPDATE rating = VALUES(rating), " +
            "                        comment = VALUES(comment), " +
            "                        created_at = NOW()";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, contentId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("review upsert failed", e);
        }
    }

    /** Reviews for a content page (joined with username). */
    public List<Review> findByContent(String contentId) {
        final String sql =
            "SELECT r.review_id, r.user_id, u.username, r.content_id, r.rating, " +
            "       r.comment, r.created_at " +
            "FROM review r JOIN users u ON u.user_id = r.user_id " +
            "WHERE r.content_id = ? " +
            "ORDER BY r.created_at DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Review> out = new ArrayList<>();
                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewId(rs.getLong(1));
                    r.setUserId(rs.getLong(2));
                    r.setUsername(rs.getString(3));
                    r.setContentId(rs.getString(4));
                    r.setRating(rs.getInt(5));
                    r.setComment(rs.getString(6));
                    Timestamp ts = rs.getTimestamp(7);
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    out.add(r);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByContent failed", e);
        }
    }

    /** Q8: Community average + count for a single piece of content. */
    public CommunityStats communityStats(String contentId) {
        final String sql =
            "SELECT AVG(r.rating) AS community_avg, COUNT(r.review_id) AS review_count " +
            "FROM content c LEFT JOIN review r ON r.content_id = c.content_id " +
            "WHERE c.content_id = ? " +
            "GROUP BY c.content_id";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contentId);
            try (ResultSet rs = ps.executeQuery()) {
                CommunityStats s = new CommunityStats();
                if (rs.next()) {
                    s.avg = rs.getBigDecimal(1);
                    s.count = rs.getInt(2);
                }
                return s;
            }
        } catch (SQLException e) {
            throw new RuntimeException("communityStats failed", e);
        }
    }

    public static class CommunityStats {
        public BigDecimal avg;
        public int count;
    }
}
