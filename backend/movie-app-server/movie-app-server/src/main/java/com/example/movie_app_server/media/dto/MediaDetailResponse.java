package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MediaDetailResponse {
    private Long id;
    private Integer tmdbId;
    private String title;
    private String overview;
    private Float voteAverage;
    private String posterUrl;
    private String backdropUrl;
    private Integer duration;
    @com.fasterxml.jackson.annotation.JsonProperty("isPremium")
    private boolean isPremium;
    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;
    private String mediaType;
    private String language;

    // --- Các trường bổ sung cho màn hình chi tiết ---
    private LocalDate releaseDate;
    private Integer releaseYear;    // Năm sản xuất (trích xuất từ releaseDate)
    private Integer viewCount;      // Lượt xem thực tế
    private Integer favoriteCount;  // Lượt yêu thích
    private String trailerUrl;      // Link trailer YouTube (embed)
    private String countryName;     // Tên quốc gia sản xuất
    private String ageRating;       // Cảnh báo độ tuổi
    private List<String> genres;    // Danh sách tên thể loại (["Hành động", "Viễn tưởng"])

    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
    private List<SeasonDto> seasons; // Danh sách các phần (dành cho phim bộ)
    private Integer expectedEpisodes; // Số tập dự kiến
}
