package com.uga.stayanalytics.model;

import java.time.LocalDateTime;

public class WatchlistEntry {
    private long userId;
    private Content content;
    private String status;
    private LocalDateTime addedAt;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
