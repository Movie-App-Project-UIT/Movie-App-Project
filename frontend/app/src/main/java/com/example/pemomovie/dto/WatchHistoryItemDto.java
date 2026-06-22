package com.example.pemomovie.dto;

public class WatchHistoryItemDto {
    private Long id;
    private Integer progressSeconds;
    private Integer totalDurationSeconds;
    private String lastWatchedAt;
    private MediaItemDto media;
    private EpisodeDto episode;

    public Long getId() { return id; }
    public Integer getProgressSeconds() { return progressSeconds; }
    public Integer getTotalDurationSeconds() { return totalDurationSeconds; }
    public String getLastWatchedAt() { return lastWatchedAt; }
    public MediaItemDto getMedia() { return media; }
    public EpisodeDto getEpisode() { return episode; }
}
