package com.example.pemomovie.dto;

import java.util.List;

public class AdminEpisodeSaveRequest {
    private String title;
    private String overview;
    private String videoUrl;
    @com.google.gson.annotations.SerializedName("isPremium")
    private boolean isPremium;
    @com.google.gson.annotations.SerializedName("isDeleted")
    private boolean isDeleted;
    private List<AdminMovieSaveRequest.AdminSubtitleRequest> subtitles;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public List<AdminMovieSaveRequest.AdminSubtitleRequest> getSubtitles() { return subtitles; }
    public void setSubtitles(List<AdminMovieSaveRequest.AdminSubtitleRequest> subtitles) { this.subtitles = subtitles; }
}
