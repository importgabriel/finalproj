-- =====================================================================
-- StayAnalytics — DDL for the OTT Movie & Series Analytics platform.
-- Run with:  mysql -u root -p < ddl.sql
-- Tables (FK-safe order): users, platform, genre, content, review,
--                         watchlist, tag, content_tag.
-- =====================================================================

DROP DATABASE IF EXISTS stayanalytics;
CREATE DATABASE stayanalytics
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE stayanalytics;

-- ---------------------------------------------------------------------
-- users: signup/login. password_hash is BCrypt ($2a$12$...).
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(64)  NOT NULL,
    email          VARCHAR(128) NOT NULL,
    password_hash  VARCHAR(72)  NOT NULL,
    role           ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email    (email)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- platform: streaming services (Netflix, Prime Video, Hotstar, ...)
-- ---------------------------------------------------------------------
CREATE TABLE platform (
    platform_id  INT          NOT NULL AUTO_INCREMENT,
    name         VARCHAR(64)  NOT NULL,
    PRIMARY KEY (platform_id),
    UNIQUE KEY uq_platform_name (name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- genre: Action, Comedy, Crime, Drama, Fantasy, Sci-Fi, Thriller
-- ---------------------------------------------------------------------
CREATE TABLE genre (
    genre_id  INT          NOT NULL AUTO_INCREMENT,
    name      VARCHAR(64)  NOT NULL,
    PRIMARY KEY (genre_id),
    UNIQUE KEY uq_genre_name (name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- content: movies and series (>= 1000 rows). content_id is a string
-- code from the source dataset (e.g. "C100146").
-- ---------------------------------------------------------------------
CREATE TABLE content (
    content_id        VARCHAR(16)  NOT NULL,
    title             VARCHAR(256) NOT NULL,
    type              ENUM('Movie','Series') NOT NULL,
    platform_id       INT          NOT NULL,
    genre_id          INT          NOT NULL,
    country           VARCHAR(64),
    language          VARCHAR(64),
    release_year      SMALLINT,
    duration_minutes  INT,
    imdb_rating       DECIMAL(3,1),
    votes             INT,
    weighted_rating   DECIMAL(4,2),
    engagement_score  DECIMAL(10,2),
    popularity_score  DECIMAL(10,2),
    trending_score    DECIMAL(10,2),
    description       TEXT,
    poster_url        VARCHAR(512),
    PRIMARY KEY (content_id),
    CONSTRAINT fk_content_platform FOREIGN KEY (platform_id) REFERENCES platform(platform_id),
    CONSTRAINT fk_content_genre    FOREIGN KEY (genre_id)    REFERENCES genre(genre_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- review: a user's rating + comment for a piece of content.
-- One review per (user, content) pair.
-- ---------------------------------------------------------------------
CREATE TABLE review (
    review_id   BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    content_id  VARCHAR(16)  NOT NULL,
    rating      TINYINT      NOT NULL,
    comment     TEXT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    UNIQUE KEY uq_review_user_content (user_id, content_id),
    CONSTRAINT fk_review_user    FOREIGN KEY (user_id)    REFERENCES users(user_id)    ON DELETE CASCADE,
    CONSTRAINT fk_review_content FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 10)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- watchlist: M:N user <-> content with status + added_at.
-- ---------------------------------------------------------------------
CREATE TABLE watchlist (
    user_id     BIGINT      NOT NULL,
    content_id  VARCHAR(16) NOT NULL,
    status      ENUM('PLAN_TO_WATCH','WATCHING','WATCHED') NOT NULL DEFAULT 'PLAN_TO_WATCH',
    added_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, content_id),
    CONSTRAINT fk_watchlist_user    FOREIGN KEY (user_id)    REFERENCES users(user_id)    ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_content FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- tag + content_tag: M:N derived from the source CSV "tags" column.
-- Used by the tag-based "similar titles" recommender (Q11).
-- ---------------------------------------------------------------------
CREATE TABLE tag (
    tag_id  INT          NOT NULL AUTO_INCREMENT,
    name    VARCHAR(64)  NOT NULL,
    PRIMARY KEY (tag_id),
    UNIQUE KEY uq_tag_name (name)
) ENGINE=InnoDB;

CREATE TABLE content_tag (
    content_id  VARCHAR(16) NOT NULL,
    tag_id      INT         NOT NULL,
    PRIMARY KEY (content_id, tag_id),
    CONSTRAINT fk_ct_content FOREIGN KEY (content_id) REFERENCES content(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_ct_tag     FOREIGN KEY (tag_id)     REFERENCES tag(tag_id)         ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Indexes (>= 2 required; we add 4 to back the queries in queries.sql).
-- See perf.txt for before/after EXPLAIN measurements.
-- ---------------------------------------------------------------------
CREATE INDEX idx_content_title          ON content(title);
CREATE INDEX idx_content_platform_genre ON content(platform_id, genre_id);
CREATE INDEX idx_review_content         ON review(content_id);
CREATE INDEX idx_content_tag_tag        ON content_tag(tag_id, content_id);
