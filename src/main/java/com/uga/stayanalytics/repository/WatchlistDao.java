package com.uga.stayanalytics.repository;

import com.uga.stayanalytics.model.Content;
import com.uga.stayanalytics.model.WatchlistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC repository for the watchlist join table. */
@Repository
public class WatchlistDao {

    private final DataSource ds;

    @Autowired
    public WatchlistDao(DataSource ds) { this.ds = ds; }

    /** Q7: Joined list for /watchlist. */
    public List<WatchlistEntry> findForUser(long userId) {
        final String sql =
            "SELECT c.content_id, c.title, c.type, c.platform_id, p.name AS platform_name, " +
            "       c.genre_id, g.name AS genre_name, c.country, c.language, " +
            "       c.release_year, c.duration_minutes, c.imdb_rating, c.votes, " +
            "       c.weighted_rating, c.engagement_score, c.popularity_score, " +
            "       c.trending_score, c.description, c.poster_url, " +
            "       w.status, w.added_at " +
            "FROM watchlist w " +
            "JOIN content c  ON c.content_id  = w.content_id " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "JOIN genre    g ON g.genre_id    = c.genre_id " +
            "WHERE w.user_id = ? " +
            "ORDER BY w.added_at DESC";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<WatchlistEntry> out = new ArrayList<>();
                while (rs.next()) {
                    Content content = new Content();
                    content.setContentId(rs.getString("content_id"));
                    content.setTitle(rs.getString("title"));
                    content.setType(rs.getString("type"));
                    content.setPlatformId(rs.getInt("platform_id"));
                    content.setPlatformName(rs.getString("platform_name"));
                    content.setGenreId(rs.getInt("genre_id"));
                    content.setGenreName(rs.getString("genre_name"));
                    content.setCountry(rs.getString("country"));
                    content.setLanguage(rs.getString("language"));
                    int y = rs.getInt("release_year"); if (!rs.wasNull()) content.setReleaseYear(y);
                    int d = rs.getInt("duration_minutes"); if (!rs.wasNull()) content.setDurationMinutes(d);
                    content.setImdbRating(rs.getBigDecimal("imdb_rating"));
                    int v = rs.getInt("votes"); if (!rs.wasNull()) content.setVotes(v);
                    content.setWeightedRating(rs.getBigDecimal("weighted_rating"));
                    content.setEngagementScore(rs.getBigDecimal("engagement_score"));
                    content.setPopularityScore(rs.getBigDecimal("popularity_score"));
                    content.setTrendingScore(rs.getBigDecimal("trending_score"));
                    content.setDescription(rs.getString("description"));
                    content.setPosterUrl(rs.getString("poster_url"));

                    WatchlistEntry e = new WatchlistEntry();
                    e.setUserId(userId);
                    e.setContent(content);
                    e.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("added_at");
                    if (ts != null) e.setAddedAt(ts.toLocalDateTime());
                    out.add(e);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findForUser failed", e);
        }
    }

    /** Insert if absent, otherwise no-op. */
    public void addIfAbsent(long userId, String contentId, String status) {
        final String sql =
            "INSERT IGNORE INTO watchlist (user_id, content_id, status, added_at) " +
            "VALUES (?, ?, ?, NOW())";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, contentId);
            ps.setString(3, status);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("addIfAbsent failed", e);
        }
    }

    /** Q3: Update status of a watchlist row. */
    public void updateStatus(long userId, String contentId, String status) {
        final String sql =
            "UPDATE watchlist SET status = ? WHERE user_id = ? AND content_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            ps.setString(3, contentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("updateStatus failed", e);
        }
    }

    /** Q4: Remove a watchlist row. */
    public void remove(long userId, String contentId) {
        final String sql =
            "DELETE FROM watchlist WHERE user_id = ? AND content_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, contentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("remove failed", e);
        }
    }

    public boolean contains(long userId, String contentId) {
        final String sql =
            "SELECT 1 FROM watchlist WHERE user_id = ? AND content_id = ? LIMIT 1";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, contentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("contains failed", e);
        }
    }
}
