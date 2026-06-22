package com.example.pemomovie.dto;

import java.util.List;

public class SeasonDto {
    private Long id;
    private Integer seasonNumber;
    private String title;
    private String overview;
    private List<EpisodeDto> episodes;

    public Long getId() { return id; }
    public Integer getSeasonNumber() { return seasonNumber; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public List<EpisodeDto> getEpisodes() { return episodes; }
}
