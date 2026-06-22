package com.example.pemomovie.dto;

import java.util.List;

public class MediaDetailResponse {
    private Long id;
    private Integer tmdbId;
    private String title;
    private String overview;
    private Float voteAverage;
    private String posterUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private Integer duration;
    private Integer viewCount;
    private Integer favoriteCount;
    @com.google.gson.annotations.SerializedName("isPremium")
    private boolean isPremium;
    @com.google.gson.annotations.SerializedName("isDeleted")
    private boolean isDeleted;
    private String mediaType;
    private java.util.List<String> genres;
    private String countryName;
    private String language;
    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
    private String trailerUrl;

    public Long getId() { return id; }
    public Integer getTmdbId() { return tmdbId; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public Float getVoteAverage() { return voteAverage; }
    public String getPosterUrl() { return posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public Integer getReleaseYear() { return releaseYear; }
    public Integer getDuration() { return duration; }
    public Integer getViewCount() { return viewCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public boolean isPremium() { return isPremium; }
    public boolean isDeleted() { return isDeleted; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public java.util.List<String> getGenres() { return genres; }
    public String getCountryName() { return countryName; }
    public String getLanguage() { return language; }
    public List<CreditDto> getDirectors() { return directors; }
    public List<CreditDto> getCast() { return cast; }
    public List<SubtitleDto> getSubtitles() { return subtitles; }
    public String getTrailerUrl() { return trailerUrl; }

    private Integer expectedEpisodes;
    private List<SeasonDto> seasons;
    
    public Integer getExpectedEpisodes() { return expectedEpisodes; }
    public List<SeasonDto> getSeasons() { return seasons; }
}
