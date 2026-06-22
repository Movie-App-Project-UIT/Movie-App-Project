package com.example.pemomovie.dto;

import java.util.List;

public class EpisodeDto {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String overview;
    private String stillUrl;
    private Integer duration;
    private boolean isPlayable;
    private List<SubtitleDto> subtitles;

    @com.google.gson.annotations.SerializedName("isPremium")
    private boolean isPremium;
    @com.google.gson.annotations.SerializedName(value = "isDeleted", alternate = {"deleted"})
    private boolean isDeleted;
    private String videoUrl;

    public Long getId() { return id; }
    public Integer getEpisodeNumber() { return episodeNumber; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getStillUrl() { return stillUrl; }
    public Integer getDuration() { return duration; }
    public boolean isPlayable() { return isPlayable; }
    public List<SubtitleDto> getSubtitles() { return subtitles; }
    public boolean isPremium() { return isPremium; }
    public boolean isDeleted() { return isDeleted; }
    public String getVideoUrl() { return videoUrl; }
    
    public void setId(Long id) { this.id = id; }
    public void setEpisodeNumber(Integer episodeNumber) { this.episodeNumber = episodeNumber; }
    public void setTitle(String title) { this.title = title; }
    public void setOverview(String overview) { this.overview = overview; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setPremium(boolean premium) { isPremium = premium; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public void setSubtitles(List<SubtitleDto> subtitles) { this.subtitles = subtitles; }
}
