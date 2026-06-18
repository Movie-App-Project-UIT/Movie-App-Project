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

    // --- Các trường bổ sung cho màn hình chi tiết ---
    private LocalDate releaseDate;
    private String mediaType;       // MOVIE hoặc TV_SERIES
    private boolean isPremium;
    private String trailerUrl;      // Link trailer YouTube (embed)
    private Integer duration;       // Thời lượng phim (phút) - dành cho phim lẻ
    private String countryName;     // Tên quốc gia sản xuất
    private List<String> genres;    // Danh sách tên thể loại (["Hành động", "Viễn tưởng"])

    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
    private List<SeasonDto> seasons; // Danh sách các phần (dành cho phim bộ)
}
