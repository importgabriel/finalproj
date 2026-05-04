==================================================================
  StayAnalytics — Cross-Platform OTT Movie & Series Analytics
  Term Project (CSCI 4370, Sami Menik, PhD)
==================================================================

Group name: StayAnalytics

Members and contributions
-------------------------
Lohith Manthena     — Database schema design (ddl.sql), data
                      pipeline (scripts/generate_movies_sql.py →
                      data.sql), indexing & performance benchmarking
                      (perf.txt).

Eshan Bhimani       — Spring Boot backend: controllers, JDBC
                      repositories, service layer (MovieService,
                      ReviewService, WatchlistService,
                      AnalyticsService), and the queries.sql
                      authoring.

Gabriel Gutierrez   — Spring Security integration, BCrypt password
                      hashing (security.txt), ER diagram,
                      normalization write-up, prelim.pdf and
                      db_design.pdf.

Gaurish Vasireddy   — Frontend Thymeleaf templates (all 8 DB-driven
                      pages), admin dashboard UI, booking-equivalent
                      "watchlist" flow UI, search/filter UI.

Technology stack
----------------
  - Java 17
  - Spring Boot 3.2 (Web, Security, Thymeleaf, JDBC)
  - MySQL 8 (run via Docker)
  - mysql-connector-j JDBC driver (PreparedStatement only)
  - Bootstrap 5 (loaded via CDN — no asset checked in)
  - Python 3 + bcrypt (used only at data-prep time to seed demo users)

No third-party libraries beyond what was permitted in Project 2.

Database connection (used in src/main/resources/application.properties)
-----------------------------------------------------------------------
  Database name : stayanalytics
  Username      : root
  Password      : root            (change to match your local MySQL
                                   root password if different — both
                                   the property file and the grader's
                                   MySQL must agree.)
  JDBC URL      : jdbc:mysql://localhost:3308/stayanalytics

Demo accounts (seeded by data.sql)
----------------------------------
  admin / Demo1!   (role ADMIN)
  alice / Demo2!   (role USER)
  bob   / Demo3!   (role USER)

How to run
----------
1. Start MySQL (Docker example):

       docker run --name stayanalytics-mysql \
            -e MYSQL_ROOT_PASSWORD=root \
            -p 3308:3306 -d mysql:8
       # If 3306 is free on your machine, you can use -p 3306:3306 instead.

2. Load the schema and the demo data:

       mysql -h 127.0.0.1 -P 3308 -uroot -proot < ddl.sql
       mysql -h 127.0.0.1 -P 3308 -uroot -proot < data.sql

3. Build & run the Spring Boot app:

       mvn spring-boot:run
       # or:
       mvn -DskipTests package
       java -jar target/stayanalytics-1.0.0.jar

4. Open http://localhost:8080/ in a browser.

How to regenerate data.sql (optional)
-------------------------------------
    pip3 install bcrypt
    python3 scripts/generate_movies_sql.py

Submission file checklist
-------------------------
   prelim.pdf            (in this zip)
   db_design.pdf         (in this zip)
   ddl.sql               ✓
   data.sql              ✓
   datasource.txt        ✓
   queries.sql           ✓
   perf.txt              ✓
   security.txt          ✓
   demo video (.mp4)     (recorded separately, included in zip)
   working code          ✓ (pom.xml, src/, scripts/)
   readme.txt            ✓ (this file)
   group member contribution form  (signed PDF, included in zip)
