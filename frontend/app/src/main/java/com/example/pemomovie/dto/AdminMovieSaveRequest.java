package com.example.pemomovie.dto;

public class AdminMovieSaveRequest {
    private Integer tmdbId;
    private String videoUrl;
    @com.google.gson.annotations.SerializedName("isPremium")
    private boolean isPremium;
    @com.google.gson.annotations.SerializedName("isDeleted")
    private boolean isDeleted;
    private String title;
    private String overview;
    private String language;
    private java.util.List<AdminSubtitleRequest> subtitles;

    public AdminMovieSaveRequest(Integer tmdbId, String videoUrl, boolean isPremium, boolean isDeleted, String title, String overview, String language, java.util.List<AdminSubtitleRequest> subtitles) {
        this.tmdbId = tmdbId;
        this.videoUrl = videoUrl;
        this.isPremium = isPremium;
        this.isDeleted = isDeleted;
        this.title = title;
        this.overview = overview;
        this.language = language;
        this.subtitles = subtitles;
    }

    public static class AdminSubtitleRequest {
        private String language;
        private String fileUrl;

        public AdminSubtitleRequest(String language, String fileUrl) {
            this.language = language;
            this.fileUrl = fileUrl;
        }

        public String getLanguage() { return language; }
        public String getFileUrl() { return fileUrl; }
    }

    private Integer expectedEpisodes;
    private String mediaType;

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public Integer getTmdbId() { return tmdbId; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { this.isPremium = premium; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getLanguage() { return language; }
    public java.util.List<AdminSubtitleRequest> getSubtitles() { return subtitles; }
    public void setSubtitles(java.util.List<AdminSubtitleRequest> subtitles) { this.subtitles = subtitles; }
    public Integer getExpectedEpisodes() { return expectedEpisodes; }
    public void setExpectedEpisodes(Integer expectedEpisodes) { this.expectedEpisodes = expectedEpisodes; }
}
