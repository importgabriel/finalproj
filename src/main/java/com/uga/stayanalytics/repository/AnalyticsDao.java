package com.uga.stayanalytics.repository;

import com.uga.stayanalytics.model.GenrePlatformStat;
import com.uga.stayanalytics.model.PlatformStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Aggregate queries for the /analytics dashboard. */
@Repository
public class AnalyticsDao {

    private final DataSource ds;

    @Autowired
    public AnalyticsDao(DataSource ds) { this.ds = ds; }

    /** Q5: per-platform avg rating + count. */
    public List<PlatformStat> perPlatform() {
        final String sql =
            "SELECT p.name AS platform_name, " +
            "       COUNT(*) AS title_count, " +
            "       AVG(c.imdb_rating) AS avg_rating, " +
            "       AVG(c.popularity_score) AS avg_popularity " +
            "FROM content c " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "GROUP BY p.name " +
            "ORDER BY avg_rating DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PlatformStat> out = new ArrayList<>();
            while (rs.next()) {
                PlatformStat s = new PlatformStat();
                s.setPlatformName(rs.getString(1));
                s.setTitleCount(rs.getLong(2));
                s.setAvgRating(rs.getBigDecimal(3));
                s.setAvgPopularity(rs.getBigDecimal(4));
                out.add(s);
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("perPlatform failed", e);
        }
    }

    /** Q6: per (genre, platform) — counts + avg rating, HAVING > threshold. */
    public List<GenrePlatformStat> perGenrePlatform(int minCount) {
        final String sql =
            "SELECT g.name AS genre_name, p.name AS platform_name, " +
            "       COUNT(*) AS cnt, AVG(c.imdb_rating) AS avg_rating " +
            "FROM content c " +
            "JOIN genre    g ON g.genre_id    = c.genre_id " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "GROUP BY g.name, p.name " +
            "HAVING cnt > ? " +
            "ORDER BY g.name ASC, avg_rating DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, minCount);
            try (ResultSet rs = ps.executeQuery()) {
                List<GenrePlatformStat> out = new ArrayList<>();
                while (rs.next()) {
                    GenrePlatformStat s = new GenrePlatformStat();
                    s.setGenreName(rs.getString(1));
                    s.setPlatformName(rs.getString(2));
                    s.setTitleCount(rs.getLong(3));
                    s.setAvgRating(rs.getBigDecimal(4));
                    out.add(s);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("perGenrePlatform failed", e);
        }
    }

    /**
     * Top community-rated titles (joins review with content).
     * Used on /analytics.
     */
    public List<TopRated> topCommunityRated(int limit, int minReviews) {
        final String sql =
            "SELECT c.content_id, c.title, p.name AS platform_name, " +
            "       AVG(r.rating) AS avg_rating, COUNT(r.review_id) AS reviews " +
            "FROM content c " +
            "JOIN review   r ON r.content_id  = c.content_id " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "GROUP BY c.content_id, c.title, p.name " +
            "HAVING reviews >= ? " +
            "ORDER BY avg_rating DESC, reviews DESC " +
            "LIMIT ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, minReviews);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<TopRated> out = new ArrayList<>();
                while (rs.next()) {
                    TopRated t = new TopRated();
                    t.contentId    = rs.getString(1);
                    t.title        = rs.getString(2);
                    t.platformName = rs.getString(3);
                    t.avgRating    = rs.getBigDecimal(4);
                    t.reviews      = rs.getLong(5);
                    out.add(t);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("topCommunityRated failed", e);
        }
    }

    public static class TopRated {
        public String contentId;
        public String title;
        public String platformName;
        public java.math.BigDecimal avgRating;
        public long reviews;
        public String getContentId() { return contentId; }
        public String getTitle() { return title; }
        public String getPlatformName() { return platformName; }
        public java.math.BigDecimal getAvgRating() { return avgRating; }
        public long getReviews() { return reviews; }
    }
}
