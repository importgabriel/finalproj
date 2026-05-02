package com.uga.stayanalytics.model;

import java.math.BigDecimal;

public class PlatformStat {
    private String platformName;
    private long titleCount;
    private BigDecimal avgRating;
    private BigDecimal avgPopularity;

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }
    public long getTitleCount() { return titleCount; }
    public void setTitleCount(long titleCount) { this.titleCount = titleCount; }
    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
    public BigDecimal getAvgPopularity() { return avgPopularity; }
    public void setAvgPopularity(BigDecimal avgPopularity) { this.avgPopularity = avgPopularity; }
}
