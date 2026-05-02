-- =====================================================================
-- StayAnalytics — queries.sql
-- All SQL statements executed by the Spring Boot app via JDBC
-- PreparedStatements. Each query is annotated with the URL that runs it.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Q1: /search filter (data retrieval, JOIN).
-- Looks up content by optional title-LIKE / genre / platform / year /
-- type. Backed by idx_content_title and idx_content_platform_genre.
-- URL: GET /search
-- ---------------------------------------------------------------------
SELECT c.*, p.name AS platform_name, g.name AS genre_name
FROM content c
JOIN platform p ON p.platform_id = c.platform_id
JOIN genre    g ON g.genre_id    = c.genre_id
WHERE (? IS NULL OR c.title LIKE ?)
  AND (? IS NULL OR c.genre_id    = ?)
  AND (? IS NULL OR c.platform_id = ?)
  AND (? IS NULL OR c.release_year = ?)
  AND (? IS NULL OR c.type        = ?)
ORDER BY c.weighted_rating DESC
LIMIT ?;

-- ---------------------------------------------------------------------
-- Q2: Submit / replace a user's review (data insertion / update).
-- Uses MySQL ON DUPLICATE KEY (UQ user_id, content_id) for upsert.
-- URL: POST /movies/{contentId}/reviews
-- ---------------------------------------------------------------------
INSERT INTO review (user_id, content_id, rating, comment, created_at)
VALUES (?, ?, ?, ?, NOW())
ON DUPLICATE KEY UPDATE rating = VALUES(rating),
                        comment = VALUES(comment),
                        created_at = NOW();

-- ---------------------------------------------------------------------
-- Q3: Update watchlist status (data update).
-- URL: POST /watchlist/{contentId}/status
-- ---------------------------------------------------------------------
UPDATE watchlist
SET    status = ?
WHERE  user_id = ? AND content_id = ?;

-- ---------------------------------------------------------------------
-- Q4: Remove a watchlist row (data deletion).
-- URL: POST /watchlist/{contentId}/remove
-- ---------------------------------------------------------------------
DELETE FROM watchlist
WHERE  user_id = ? AND content_id = ?;

-- ---------------------------------------------------------------------
-- Q5: Per-platform aggregate (AGGREGATION + JOIN).
-- Counts and averages titles per streaming platform.
-- URL: GET /analytics
-- ---------------------------------------------------------------------
SELECT p.name AS platform_name,
       COUNT(*) AS title_count,
       AVG(c.imdb_rating) AS avg_rating,
       AVG(c.popularity_score) AS avg_popularity
FROM content c
JOIN platform p ON p.platform_id = c.platform_id
GROUP BY p.name
ORDER BY avg_rating DESC;

-- ---------------------------------------------------------------------
-- Q6: Per (genre, platform) aggregate (AGGREGATION + JOIN + HAVING).
-- URL: GET /analytics
-- ---------------------------------------------------------------------
SELECT g.name AS genre_name,
       p.name AS platform_name,
       COUNT(*) AS cnt,
       AVG(c.imdb_rating) AS avg_rating
FROM content c
JOIN genre    g ON g.genre_id    = c.genre_id
JOIN platform p ON p.platform_id = c.platform_id
GROUP BY g.name, p.name
HAVING cnt > ?
ORDER BY g.name ASC, avg_rating DESC;

-- ---------------------------------------------------------------------
-- Q7: User's watchlist with content metadata (JOIN).
-- URL: GET /watchlist
-- ---------------------------------------------------------------------
SELECT c.content_id, c.title, c.type, c.platform_id, p.name AS platform_name,
       c.genre_id, g.name AS genre_name, c.country, c.language,
       c.release_year, c.duration_minutes, c.imdb_rating, c.votes,
       c.weighted_rating, c.engagement_score, c.popularity_score,
       c.trending_score, c.description, c.poster_url,
       w.status, w.added_at
FROM watchlist w
JOIN content c  ON c.content_id  = w.content_id
JOIN platform p ON p.platform_id = c.platform_id
JOIN genre    g ON g.genre_id    = c.genre_id
WHERE  w.user_id = ?
ORDER BY w.added_at DESC;

-- ---------------------------------------------------------------------
-- Q8: Community average per title (AGGREGATION + LEFT JOIN).
-- Backed by idx_review_content.
-- URL: GET /movies/{contentId}
-- ---------------------------------------------------------------------
SELECT AVG(r.rating) AS community_avg,
       COUNT(r.review_id) AS review_count
FROM   content c
LEFT JOIN review r ON r.content_id = c.content_id
WHERE  c.content_id = ?
GROUP BY c.content_id;

-- ---------------------------------------------------------------------
-- Q9: Sign up — insert user with BCrypt-hashed password.
-- URL: POST /signup
-- ---------------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role, created_at)
VALUES (?, ?, ?, ?, NOW());

-- ---------------------------------------------------------------------
-- Q10: Login lookup (data retrieval) — used by JdbcUserDetailsService.
-- URL: POST /login (Spring Security form submission)
-- ---------------------------------------------------------------------
SELECT user_id, username, email, password_hash, role, created_at
FROM   users
WHERE  username = ?;

-- ---------------------------------------------------------------------
-- Q11: Tag-based "similar titles" recommender (AGG + 2x JOIN).
-- Backed by idx_content_tag_tag.
-- URL: GET /movies/{contentId}
-- ---------------------------------------------------------------------
SELECT c2.content_id, c2.title, c2.poster_url, COUNT(*) AS shared_tags
FROM   content_tag t1
JOIN   content_tag t2 ON t1.tag_id = t2.tag_id
                     AND t1.content_id <> t2.content_id
JOIN   content c2     ON c2.content_id = t2.content_id
WHERE  t1.content_id = ?
GROUP BY c2.content_id, c2.title, c2.poster_url
ORDER BY shared_tags DESC, c2.title ASC
LIMIT ?;

-- ---------------------------------------------------------------------
-- Q12: Top community-rated titles for /analytics (AGG + 2x JOIN).
-- URL: GET /analytics
-- ---------------------------------------------------------------------
SELECT c.content_id, c.title, p.name AS platform_name,
       AVG(r.rating) AS avg_rating, COUNT(r.review_id) AS reviews
FROM   content c
JOIN   review   r ON r.content_id  = c.content_id
JOIN   platform p ON p.platform_id = c.platform_id
GROUP BY c.content_id, c.title, p.name
HAVING reviews >= ?
ORDER BY avg_rating DESC, reviews DESC
LIMIT ?;
