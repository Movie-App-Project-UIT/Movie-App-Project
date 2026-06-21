package com.example.movie_app_server.media.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbTvDetailsDto {
    private Integer id;
    private String name; // Phim bộ TMDB dùng "name" thay vì "title"
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("vote_average")
    private Float voteAverage;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_name")
    private String originalName;

    private List<TmdbSeasonBasicDto> seasons; // Chứa danh sách các phần
    private TmdbVideosDto videos;

    private java.util.List<TmdbGenreResponseDto.TmdbGenreDto> genres;

    @JsonProperty("production_countries")
    private java.util.List<TmdbMovieDetailsDto.TmdbProductionCountryDto> productionCountries;

    @Data
    public static class TmdbSeasonBasicDto {
        @JsonProperty("season_number")
        private Integer seasonNumber;
    }
}