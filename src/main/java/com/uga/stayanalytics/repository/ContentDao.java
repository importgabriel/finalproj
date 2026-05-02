package com.uga.stayanalytics.repository;

import com.uga.stayanalytics.model.Content;
import com.uga.stayanalytics.model.Genre;
import com.uga.stayanalytics.model.Platform;
import com.uga.stayanalytics.model.SimilarTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC repository for content/platform/genre/tag (read-mostly). */
@Repository
public class ContentDao {

    private final DataSource ds;

    @Autowired
    public ContentDao(DataSource ds) { this.ds = ds; }

    /** Single content row joined with platform + genre name. */
    public Optional<Content> findById(String contentId) {
        final String sql =
            "SELECT c.*, p.name AS platform_name, g.name AS genre_name " +
            "FROM content c " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "JOIN genre    g ON g.genre_id    = c.genre_id " +
            "WHERE c.content_id = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("findById failed", e);
        }
    }

    /** /home — top trending. */
    public List<Content> findTrending(int limit) {
        final String sql =
            "SELECT c.*, p.name AS platform_name, g.name AS genre_name " +
            "FROM content c " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "JOIN genre    g ON g.genre_id    = c.genre_id " +
            "ORDER BY c.trending_score DESC LIMIT ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Content> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findTrending failed", e);
        }
    }

    /**
     * Q1: /search filter. Any of the args may be null/empty → ignored.
     * Uses idx_content_title and idx_content_platform_genre.
     */
    public List<Content> search(String title, Integer genreId, Integer platformId,
                                Integer year, String type, int limit) {
        final String sql =
            "SELECT c.*, p.name AS platform_name, g.name AS genre_name " +
            "FROM content c " +
            "JOIN platform p ON p.platform_id = c.platform_id " +
            "JOIN genre    g ON g.genre_id    = c.genre_id " +
            "WHERE (? IS NULL OR c.title LIKE ?) " +
            "  AND (? IS NULL OR c.genre_id = ?) " +
            "  AND (? IS NULL OR c.platform_id = ?) " +
            "  AND (? IS NULL OR c.release_year = ?) " +
            "  AND (? IS NULL OR c.type = ?) " +
            "ORDER BY c.weighted_rating DESC " +
            "LIMIT ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = (title == null || title.isBlank()) ? null : "%" + title + "%";
            setNullableString(ps, 1, like);
            setNullableString(ps, 2, like);
            setNullableInt(ps, 3, genreId);
            setNullableInt(ps, 4, genreId);
            setNullableInt(ps, 5, platformId);
            setNullableInt(ps, 6, platformId);
            setNullableInt(ps, 7, year);
            setNullableInt(ps, 8, year);
            String t = (type == null || type.isBlank()) ? null : type;
            setNullableString(ps, 9, t);
            setNullableString(ps, 10, t);
            ps.setInt(11, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Content> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("search failed", e);
        }
    }

    public List<Platform> allPlatforms() {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT platform_id, name FROM platform ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            List<Platform> out = new ArrayList<>();
            while (rs.next()) out.add(new Platform(rs.getInt(1), rs.getString(2)));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("allPlatforms failed", e);
        }
    }

    public List<Genre> allGenres() {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT genre_id, name FROM genre ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            List<Genre> out = new ArrayList<>();
            while (rs.next()) out.add(new Genre(rs.getInt(1), rs.getString(2)));
            return out;
        } catch (SQLException e) {
            throw new RuntimeException("allGenres failed", e);
        }
    }

    /**
     * Q11: Tag-based similar titles. Uses idx_content_tag_tag.
     */
    public List<SimilarTitle> similarByTags(String contentId, int limit) {
        final String sql =
            "SELECT c2.content_id, c2.title, c2.poster_url, g.name AS genre_name, " +
            "       COUNT(*) AS shared_tags " +
            "FROM content_tag t1 " +
            "JOIN content_tag t2 ON t1.tag_id = t2.tag_id AND t1.content_id <> t2.content_id " +
            "JOIN content c2 ON c2.content_id = t2.content_id " +
            "JOIN genre   g  ON g.genre_id   = c2.genre_id " +
            "WHERE t1.content_id = ? " +
            "GROUP BY c2.content_id, c2.title, c2.poster_url, g.name " +
            "ORDER BY shared_tags DESC, c2.title ASC " +
            "LIMIT ?";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contentId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<SimilarTitle> out = new ArrayList<>();
                while (rs.next()) {
                    SimilarTitle s = new SimilarTitle();
                    s.setContentId(rs.getString(1));
                    s.setTitle(rs.getString(2));
                    s.setPosterUrl(rs.getString(3));
                    s.setGenreName(rs.getString(4));
                    s.setSharedTags(rs.getInt(5));
                    out.add(s);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("similarByTags failed", e);
        }
    }

    public List<String> tagsFor(String contentId) {
        final String sql =
            "SELECT t.name FROM content_tag ct " +
            "JOIN tag t ON t.tag_id = ct.tag_id " +
            "WHERE ct.content_id = ? ORDER BY t.name";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> out = new ArrayList<>();
                while (rs.next()) out.add(rs.getString(1));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException("tagsFor failed", e);
        }
    }

    private static void setNullableString(PreparedStatement ps, int idx, String v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.VARCHAR); else ps.setString(idx, v);
    }
    private static void setNullableInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) ps.setNull(idx, Types.INTEGER); else ps.setInt(idx, v);
    }

    private Content map(ResultSet rs) throws SQLException {
        Content c = new Content();
        c.setContentId(rs.getString("content_id"));
        c.setTitle(rs.getString("title"));
        c.setType(rs.getString("type"));
        c.setPlatformId(rs.getInt("platform_id"));
        c.setPlatformName(rs.getString("platform_name"));
        c.setGenreId(rs.getInt("genre_id"));
        c.setGenreName(rs.getString("genre_name"));
        c.setCountry(rs.getString("country"));
        c.setLanguage(rs.getString("language"));
        int y = rs.getInt("release_year"); if (!rs.wasNull()) c.setReleaseYear(y);
        int d = rs.getInt("duration_minutes"); if (!rs.wasNull()) c.setDurationMinutes(d);
        c.setImdbRating(rs.getBigDecimal("imdb_rating"));
        int v = rs.getInt("votes"); if (!rs.wasNull()) c.setVotes(v);
        c.setWeightedRating(rs.getBigDecimal("weighted_rating"));
        c.setEngagementScore(rs.getBigDecimal("engagement_score"));
        c.setPopularityScore(rs.getBigDecimal("popularity_score"));
        c.setTrendingScore(rs.getBigDecimal("trending_score"));
        c.setDescription(rs.getString("description"));
        c.setPosterUrl(rs.getString("poster_url"));
        return c;
    }
}
