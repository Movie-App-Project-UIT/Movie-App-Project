package com.example.pemomovie.dto;

import java.io.Serializable;

public class MediaItemDto implements Serializable {
    private Long id;
    private String title;
    private String posterUrl;
    private String backdropUrl;
    private Float voteAverage;
    @com.google.gson.annotations.SerializedName("isPremium")
    private boolean isPremium;  // Để App hiện cái mác "PREMIUM" màu đen góc phải
    private String mediaType;   // Trả về "MOVIE" hoặc "TV_SHOW" để App phân biệt
    @com.google.gson.annotations.SerializedName("isPlayable")
    private boolean isPlayable; // Bằng true nếu phim đã có videoUrl, App có thể làm mờ nút Play nếu false
    @com.google.gson.annotations.SerializedName(value = "isDeleted", alternate = {"deleted"})
    private boolean isDeleted; // mapping to isDeleted from backend
    private java.util.List<String> genres;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public void setBackdropUrl(String backdropUrl) {
        this.backdropUrl = backdropUrl;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isPlayable() {
        return isPlayable;
    }

    public void setPlayable(boolean playable) {
        isPlayable = playable;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public java.util.List<String> getGenres() {
        return genres;
    }

    public void setGenres(java.util.List<String> genres) {
        this.genres = genres;
    }

    private String language;
    private String country;
    private Integer viewCount;
    private Long hiddenByGenreId;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Long getHiddenByGenreId() { return hiddenByGenreId; }
    public void setHiddenByGenreId(Long hiddenByGenreId) { this.hiddenByGenreId = hiddenByGenreId; }

    private Integer duration;
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
