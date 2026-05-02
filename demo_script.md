# StayAnalytics — Demo Video Script

**Target length:** 8–10 minutes
**Tools:** screen recorder (QuickTime / OBS), terminal, browser at `http://localhost:8080`, MySQL CLI tab

> Each member states their **name** at the start of their section.
> Pre-record each section, then stitch — that's easier than a single-take.

---

## Pre-recording checklist (do once before pressing record)

- [ ] Docker MySQL container `stayanalytics-mysql` running on port 3307
- [ ] Schema + data loaded:
      `mysql -h 127.0.0.1 -P 3307 -uroot -proot stayanalytics -e "SELECT COUNT(*) FROM content;"`
      → expect **2500**
- [ ] Spring Boot app running on `http://localhost:8080`
- [ ] Two browser windows ready: one logged out, one logged in as `alice/Demo2!`
- [ ] Terminal split into 3 panes: (1) `mysql` CLI, (2) `java -jar …` log tail, (3) free for commands
- [ ] Editor open with `queries.sql`, `perf.txt`, `security.txt` ready to scroll
- [ ] Close everything else, mute notifications, set zoom level so text is legible

---

## Section 1 — **Lohith Manthena** · Setup, schema, data (≈ 2 min)

> "Hi, I'm **Lohith Manthena**. I'll walk through how StayAnalytics is set up and what's in the database."

1. **Start MySQL container** (already running — show, don't run live):
   ```bash
   docker ps --format '{{.Names}} -> {{.Ports}}'
   # stayanalytics-mysql -> 0.0.0.0:3307->3306/tcp
   ```

2. **Show the DDL** — open `ddl.sql`, scroll quickly:
   - Point out the **8 tables** in FK-safe order:
     `users → platform → genre → content → review → watchlist → tag → content_tag`
   - Point out the **4 indexes** at the bottom.
   - Mention: `content` table will hold all 2,500 movies.

3. **Show the data loader** — open `scripts/generate_movies_sql.py`:
   - Reads `ott_movies_clean_unique.csv` → emits `data.sql`.
   - Also seeds 3 demo users with **BCrypt-hashed** passwords, 5,000 reviews,
     500 watchlist rows.

4. **Verify counts in MySQL**:
   ```sql
   SELECT (SELECT COUNT(*) FROM content)   AS movies,
          (SELECT COUNT(*) FROM users)     AS users,
          (SELECT COUNT(*) FROM review)    AS reviews,
          (SELECT COUNT(*) FROM watchlist) AS watchlist;
   ```
   Expect: `2500 / 3 / 5000 / 500`. ✅ Meets the "≥ 1000 rows in one table" requirement.

5. **Boot the app** (already running — show the running log):
   ```bash
   mvn spring-boot:run    # or: java -jar target/stayanalytics-1.0.0.jar
   ```
   Tomcat starts on port 8080. "Hand off to Eshan."

---

## Section 2 — **Eshan Bhimani** · Backend & queries (≈ 2 min)

> "Hi, I'm **Eshan Bhimani**. I built the Spring Boot backend and authored the SQL."

1. **Project layout** — open the Maven project tree:
   - `controller/` → 7 `@Controller` classes (Auth, Home, Search, Movie, Watchlist, Analytics, Admin)
   - `service/` → 5 `@Service` classes
   - `repository/` → 5 JDBC DAOs (UserDao, ContentDao, ReviewDao, WatchlistDao, AnalyticsDao)
   - **Every** DAO method uses `try-with-resources + PreparedStatement + setX(...)`
     — no string concatenation, no raw `Statement`. SQL injection is eliminated.

2. **Walk through `queries.sql`** — there are **12 queries** total; ≥ 6 with non-trivial joins/aggregation:
   - **Q1** — `/search` filter (multi-condition WHERE, JOIN platform + genre).
   - **Q2** — `/movies/{id}` POST review (INSERT … ON DUPLICATE KEY UPDATE — upsert).
   - **Q3 / Q4** — `/watchlist` UPDATE status, DELETE row.
   - **Q5** — `/analytics` per-platform `COUNT(*) + AVG(imdb_rating)` with JOIN.
   - **Q6** — `/analytics` per (genre, platform) — `GROUP BY` two columns, `HAVING cnt > ?`.
   - **Q7** — `/watchlist` user's saved films, JOIN platform + genre.
   - **Q8** — `/movies/{id}` per-title community AVG via LEFT JOIN.
   - **Q11** — `/movies/{id}` similar titles via self-join on `content_tag`.
   - **Q12** — `/analytics` top community-rated, AVG + COUNT + JOIN platform.

3. **Show a query running live** (in MySQL CLI):
   ```sql
   SELECT p.name, COUNT(*) titles, ROUND(AVG(c.imdb_rating),2) avg_rating
   FROM content c JOIN platform p USING(platform_id)
   GROUP BY p.name ORDER BY avg_rating DESC;
   ```
   "This is exactly what `/analytics` runs through `AnalyticsDao.perPlatform()`."

4. "Hand off to Gabriel for security."

---

## Section 3 — **Gabriel Gutierrez** · Auth & BCrypt (≈ 2 min)

> "Hi, I'm **Gabriel Gutierrez**. I integrated Spring Security and wrote the password-hashing layer."

1. **Show `security.txt`** briefly — point out:
   - `BCryptPasswordEncoder(12)` registered as a Spring bean.
   - 12 = work factor. 2^12 internal iterations ≈ 250 ms per hash.
   - Salt is embedded inside the hash (`$2a$12$<salt><hash>`).

2. **Show `SecurityConfig.java`**:
   - `formLogin()` with custom `/login` page.
   - `JdbcUserDetailsService` runs Q10 (`SELECT … FROM users WHERE username = ?`).
   - Public endpoints listed; `/watchlist` and write endpoints require auth;
     `/admin` requires `ROLE_ADMIN`.

3. **Sign up a brand-new user live** in the browser:
   - Go to `/signup` → fill in `demo_user / demo@example.com / Demo1234`.
   - Submit. Get redirected to `/login?registered`.
   - In MySQL CLI:
     ```sql
     SELECT user_id, username, email, role, SUBSTRING(password_hash,1,7) AS prefix
     FROM users WHERE username='demo_user';
     ```
     Show: `prefix = $2a$12$` — confirming the password is BCrypt-hashed, **never** stored as plaintext.

4. **Log in** as the new user from `/login`. Show the navbar updates with the username + Sign out button.

5. **Show ER + normalization briefly** — flip to `db_design.pdf`:
   - 7 entity sets, decomposed to BCNF.
   - "Hand off to Gaurish for the UI tour."

---

## Section 4 — **Gaurish Vasireddy** · UI tour (≈ 3 min)

> "Hi, I'm **Gaurish Vasireddy**. I'll demo all 8 pages of the UI."

Sign in as `alice / Demo2!` if not already. Visit each page in order:

1. **`/home`** — popular this week.
   - Filter bar at the top ("Browse by year / genre / service / type").
   - Hero 4-up grid with stat icons (eye = views, list = lists, heart = likes).
   - Promo banner linking to `/analytics`.
   - Secondary "Just discovered" 8-up compact grid.
   - Each thumbnail is a **genre-colored monogram** — drama is purple, action is red, sci-fi is cyan, etc. Same title always gets the same color.

2. **`/search?title=Silent`** — hits **Q1**.
   - Type "Silent" → submit.
   - Combine with a service filter → submit again. Show result count drops.

3. **`/movies/C100146`** — hits **Q8 + Q11**.
   - Big monogram poster on the left, metric cards (IMDb / Community / Trending), tag pills, description.
   - "Similar films" carousel (Q11 — self-join on `content_tag`).
   - Reviews list. Submit a new review — rating 9, "Great visuals." → row appears, `created_at` updates. (Demonstrates Q2 upsert if the user re-rates.)

4. **`/watchlist`** — hits **Q7 / Q3 / Q4**.
   - Click "+ Add to watchlist" on the detail page first → returns to detail with a green flash message.
   - Navigate to `/watchlist` → row appears in the table.
   - Change the status from "Plan to watch" → "Watched" → click Save (Q3 UPDATE).
   - Click "Remove" on a row → confirm row disappears (Q4 DELETE).

5. **`/analytics`** — hits **Q5 / Q6 / Q12**.
   - Average rating per service, with bar fills.
   - Top community-rated titles (links jump back to detail pages).
   - Genre × service breakdown (HAVING cnt > 5).

6. **`/admin`** — sign out, sign back in as `admin / Demo1!`.
   - Shows ADMIN role-only page (catalog by service + top trending).
   - Demonstrates role-based `requestMatchers("/admin/**").hasRole("ADMIN")`.

7. **Sign out** → confirm `/watchlist` now redirects back to `/login`. CSRF + session ended.

"Hand back for the closing performance demo."

---

## Section 5 — **Anyone** · Index performance (≈ 1 min)

> "And finally, here's how indexing matters."

Open `perf.txt`, then run live in MySQL:

```sql
EXPLAIN SELECT c.* FROM content c
WHERE c.title LIKE 'Silent%' ORDER BY c.weighted_rating DESC LIMIT 50;
```

Show:
- `key = idx_content_title`, `type = range`, `rows ~ 80` — the index is being used.
- Then: `DROP INDEX idx_content_title ON content;` rerun → `type = ALL, rows = 2500`.
- Recreate: `CREATE INDEX idx_content_title ON content(title);`.

Point at the wall-clock numbers in `perf.txt` (8× / 14× / 7× speedups for Q1 / Q8 / Q11).

> "That covers the database, the backend, the security model, the UI, and indexing. Thanks for watching."

---

## Closing checklist (after recording)

- [ ] Verify audio level on each section.
- [ ] Trim dead air at section boundaries.
- [ ] Export as `demo.mp4` (H.264, 1080p, ~ 60 MB or less).
- [ ] Drop into the submission zip alongside `prelim.pdf`, `db_design.pdf`, `ddl.sql`,
      `data.sql`, `datasource.txt`, `queries.sql`, `perf.txt`, `security.txt`,
      `readme.txt`, signed `contribution-form.pdf`, `pom.xml`, `src/`, `scripts/`.
- [ ] Re-extract the zip on a different machine and run the load + boot sequence
      from the readme to confirm the deliverable is self-contained.
