package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MediaDetailResponse {
    private Long id;
    private String title;
    private String overview;
    private Float voteAverage;
    private String posterUrl;
    private String backdropUrl;
    private Integer duration;
    private boolean isPremium;
    private String mediaType;
    private String language;

    // --- Các trường bổ sung cho màn hình chi tiết ---
    private LocalDate releaseDate;
    private String trailerUrl;      // Link trailer YouTube (embed)
    private String countryName;     // Tên quốc gia sản xuất
    private String ageRating;       // Cảnh báo độ tuổi
    private List<String> genres;    // Danh sách tên thể loại (["Hành động", "Viễn tưởng"])

    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
    private List<SeasonDto> seasons; // Danh sách các phần (dành cho phim bộ)
}
