package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaItemDto {
    private Long id;
    private String title;
    private String posterUrl;
    private String backdropUrl; // Ảnh ngang cho danh sách cuộn to
    private Float voteAverage;
    @com.fasterxml.jackson.annotation.JsonProperty("isPremium")
    private boolean isPremium;  // Để App hiện cái mác "PREMIUM" màu đen góc phải
    private String mediaType;   // Trả về "MOVIE" hoặc "TV_SHOW" để App phân biệt
    @com.fasterxml.jackson.annotation.JsonProperty("isPlayable")
    private boolean isPlayable; // Bằng true nếu phim đã có videoUrl, App có thể làm mờ nút Play nếu false
    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;
    private java.util.List<String> genres;
    private String language;
    private String country;
    private Integer viewCount;
    private Long hiddenByGenreId;
}