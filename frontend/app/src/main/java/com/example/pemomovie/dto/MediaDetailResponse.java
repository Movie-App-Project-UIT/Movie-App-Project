package com.example.pemomovie.dto;

import java.util.List;

public class MediaDetailResponse {
    private Long id;
    private String title;
    private String overview;
    private Float voteAverage;
    private String posterUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private Integer viewCount;
    private boolean isPremium;
    private String mediaType;
    private String genre;
    private String country;
    private String language;
    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public Float getVoteAverage() { return voteAverage; }
    public String getPosterUrl() { return posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public Integer getReleaseYear() { return releaseYear; }
    public Integer getDuration() { return duration; }
    public Integer getViewCount() { return viewCount; }
    public boolean isPremium() { return isPremium; }
    public String getMediaType() { return mediaType; }
    public String getGenre() { return genre; }
    public String getCountry() { return country; }
    public String getLanguage() { return language; }
    public List<CreditDto> getDirectors() { return directors; }
    public List<CreditDto> getCast() { return cast; }
    public List<SubtitleDto> getSubtitles() { return subtitles; }
}
