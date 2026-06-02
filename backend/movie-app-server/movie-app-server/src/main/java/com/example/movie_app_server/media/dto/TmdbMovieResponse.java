package com.example.movie_app_server.media.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieResponse {
    private Integer id;
    private String title;
    private String overview;
    private String poster_path;
    private String backdrop_path;
    private String release_date;
    private Float vote_average;
    private List<GenreDto> genres;

    private Integer runtime;
    private List<CountryDto> production_countries;
    private CreditsDto credits;
    private VideosDto videos;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenreDto {
        private Integer id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryDto {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditsDto {
        private List<CastDto> cast;
        private List<CrewDto> crew;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CastDto {
        private Integer id;
        private String name;
        private String character;
        private String profile_path;
        private String known_for_department;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrewDto {
        private Integer id;
        private String name;
        private String job;
        private String department;
        private String profile_path;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideosDto {
        private List<VideoResultDto> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoResultDto {
        private String site;
        private String type;
        private String key;
    }
}
