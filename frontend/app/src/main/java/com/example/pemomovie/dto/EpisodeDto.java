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

    public Long getId() { return id; }
    public Integer getEpisodeNumber() { return episodeNumber; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getStillUrl() { return stillUrl; }
    public Integer getDuration() { return duration; }
    public boolean isPlayable() { return isPlayable; }
    public List<SubtitleDto> getSubtitles() { return subtitles; }
}
