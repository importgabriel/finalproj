package com.uga.stayanalytics.model;

public class SimilarTitle {
    private String contentId;
    private String title;
    private String posterUrl;
    private String genreName;
    private int sharedTags;

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
    public int getSharedTags() { return sharedTags; }
    public void setSharedTags(int sharedTags) { this.sharedTags = sharedTags; }

    public String getGenreSlug() {
        if (genreName == null) return "default";
        return genreName.toLowerCase()
                        .replace("-", "")
                        .replace(" ", "");
    }

    public String getMonogramInitial() {
        if (title == null || title.isEmpty()) return "?";
        return title.substring(0, 1).toUpperCase();
    }
}
