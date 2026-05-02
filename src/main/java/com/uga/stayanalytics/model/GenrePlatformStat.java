package com.uga.stayanalytics.model;

import java.math.BigDecimal;

public class GenrePlatformStat {
    private String genreName;
    private String platformName;
    private long titleCount;
    private BigDecimal avgRating;

    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public long getTitleCount() { return titleCount; }
    public void setTitleCount(long titleCount) { this.titleCount = titleCount; }
    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
}
