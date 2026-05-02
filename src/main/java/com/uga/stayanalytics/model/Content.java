package com.uga.stayanalytics.model;

import java.math.BigDecimal;

public class Content {
    private String contentId;
    private String title;
    private String type;
    private int platformId;
    private String platformName;
    private int genreId;
    private String genreName;
    private String country;
    private String language;
    private Integer releaseYear;
    private Integer durationMinutes;
    private BigDecimal imdbRating;
    private Integer votes;
    private BigDecimal weightedRating;
    private BigDecimal engagementScore;
    private BigDecimal popularityScore;
    private BigDecimal trendingScore;
    private String description;
    private String posterUrl;

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getPlatformId() { return platformId; }
    public void setPlatformId(int platformId) { this.platformId = platformId; }
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public int getGenreId() { return genreId; }
    public void setGenreId(int genreId) { this.genreId = genreId; }
    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public BigDecimal getImdbRating() { return imdbRating; }
    public void setImdbRating(BigDecimal imdbRating) { this.imdbRating = imdbRating; }
    public Integer getVotes() { return votes; }
    public void setVotes(Integer votes) { this.votes = votes; }
    public BigDecimal getWeightedRating() { return weightedRating; }
    public void setWeightedRating(BigDecimal weightedRating) { this.weightedRating = weightedRating; }
    public BigDecimal getEngagementScore() { return engagementScore; }
    public void setEngagementScore(BigDecimal engagementScore) { this.engagementScore = engagementScore; }
    public BigDecimal getPopularityScore() { return popularityScore; }
    public void setPopularityScore(BigDecimal popularityScore) { this.popularityScore = popularityScore; }
    public BigDecimal getTrendingScore() { return trendingScore; }
    public void setTrendingScore(BigDecimal trendingScore) { this.trendingScore = trendingScore; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getGenreSlug() {
        if (genreName == null) return "default";
        return genreName.toLowerCase()
                        .replace("-", "")
                        .replace(" ", "");
    }

    /** 0–2: small variation within a genre, used to nudge the gradient angle. */
    public int getMonogramShade() {
        if (contentId == null) return 0;
        int sum = 0;
        for (int i = 0; i < contentId.length(); i++) sum += contentId.charAt(i);
        return Math.floorMod(sum, 3);
    }

    public String getMonogramInitial() {
        if (title == null || title.isEmpty()) return "?";
        return title.substring(0, 1).toUpperCase();
    }
}
