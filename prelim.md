# StayAnalytics — Preliminary Project Proposal

**Group:** StayAnalytics
**Members:** Lohith Manthena, Eshan Bhimani, Gabriel Gutierrez, Gaurish Vasireddy
**Course:** CSCI 4370 — Term Project (Instructor: Sami Menik, PhD)

> Convert this Markdown file to **prelim.pdf** before submission
> (e.g. `pandoc prelim.md -o prelim.pdf` or print-to-PDF from a viewer).

---

## 1. Project title

**StayAnalytics — Cross-Platform OTT Movie & Series Analytics**

## 2. Problem and domain

Streaming-video subscribers and casual analysts increasingly juggle
multiple OTT platforms (Netflix, Prime Video, Hotstar, ...). Each
platform maintains its own opaque ranking ("trending now", "top 10")
that is not directly comparable across services, and most platforms do
not expose a *community* rating distinct from third-party sources like
IMDb. As a result:

- Discovering high-quality content requires hopping between apps.
- It is hard to compare, say, Netflix's drama catalog against Prime
  Video's, or to see which platform has the highest average user
  rating in a given genre.
- Personal "to-watch" lists are siloed inside each app.

This project addresses those gaps with a single web application backed
by a unified relational database of titles, platforms, and community
reviews.

## 3. Solution

**StayAnalytics** is a Spring Boot + Thymeleaf web app where any user
can:

- **Browse** trending titles across platforms (`/home`).
- **Search/filter** by title, genre, platform, year, and type
  (`/search`).
- **View detail** for any title — IMDb metadata, our community rating,
  tag-based "similar titles" recommendations, and the review feed
  (`/movies/{id}`).

Authenticated users additionally:

- **Sign up / log in** with passwords stored as BCrypt hashes.
- **Maintain a personal watchlist** with status `PLAN_TO_WATCH /
  WATCHING / WATCHED` (`/watchlist`).
- **Rate and review** any title; one review per (user, title) pair
  with upsert semantics.

Anyone can open a cross-platform **analytics dashboard** that surfaces
aggregate insights: average rating per platform, genre×platform
breakdown, and top community-rated titles (`/analytics`). An admin
role unlocks an additional `/admin` summary page.

## 4. Preliminary ER diagram

```
+--------+        +-------------+       +-----------+
| users  |---<reviews>---       |       |  platform |
+--------+         |            |       +-----------+
    |              v            |             |
    |         +----------+      |             v
    |         |  review  |      |       +-----------+
    |         +----------+      |       |  content  |---N:1--->+ genre +
    |              ^            |       +-----------+          +-------+
    |              |            |             |
    |     +----------+          |             v
    +-N:M-| watchlist|----N:1---+       +-----------+    +-----+
          +----------+                  |content_tag|N:M-| tag |
                                        +-----------+    +-----+
```

**Entity sets** (≥ 4 required):
1. `users` — accounts with role USER/ADMIN.
2. `platform` — Netflix, Prime Video, Hotstar.
3. `genre` — Action, Comedy, Crime, Drama, Fantasy, Sci-Fi, Thriller.
4. `content` — movies and series (2,500 rows).
5. `review` — user × content × rating × comment.
6. `watchlist` — user × content × status.
7. `tag` and `content_tag` — derived from the source CSV's `tags`
   column to power "similar titles".

**Relationships:**
- `users` 1—N `review` N—1 `content`
- `users` N—M `content` (via `watchlist`)
- `content` N—1 `platform`, `content` N—1 `genre`
- `content` N—M `tag` (via `content_tag`)

The full ER → relation mapping, FDs, and BCNF normalization are in
`db_design.pdf`.

## 5. Technologies

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 (Web, Security, Thymeleaf, JDBC) |
| Database | MySQL 8 in Docker |
| DB driver | mysql-connector-j (JDBC PreparedStatement only) |
| Templating | Thymeleaf + Bootstrap 5 |
| Auth | Spring Security `formLogin` + `BCryptPasswordEncoder(12)` |
| Data prep | Python 3 + bcrypt (one-shot to produce data.sql) |

No third-party libraries beyond what Project 2 already permitted.

## 6. User interfaces (≥ 5 required; we ship 8)

1. `/signup` — sign-up form
2. `/login` — login form
3. `/home` — trending titles
4. `/search` — search & filter results
5. `/movies/{id}` — title detail + reviews + similar titles
6. `/watchlist` — user's saved titles
7. `/analytics` — cross-platform analytics dashboard
8. `/admin` — admin-role summary page

Every page's primary content is generated from a SQL query against
the `stayanalytics` database; the full query catalog lives in
`queries.sql`.
