package com.example.movie_app_server.media.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbCreditsResponseDto {
    private Integer id;
    private List<TmdbCastDto> cast;
    private List<TmdbCrewDto> crew;

    // --- Class con hứng thông tin Diễn viên ---
    @Data
    public static class TmdbCastDto {
        private Integer id;
        private String name;
        private String character;

        @JsonProperty("profile_path")
        private String profilePath;

        @JsonProperty("known_for_department")
        private String knownForDepartment;
    }

    // --- Class con hứng thông tin Đạo diễn/Đoàn làm phim ---
    @Data
    public static class TmdbCrewDto {
        private Integer id;
        private String name;
        private String job;

        @JsonProperty("profile_path")
        private String profilePath;

        @JsonProperty("department")
        private String department;
    }
}