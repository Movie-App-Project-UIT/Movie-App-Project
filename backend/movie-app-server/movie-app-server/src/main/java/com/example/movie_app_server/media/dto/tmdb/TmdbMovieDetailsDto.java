package com.example.movie_app_server.media.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TmdbMovieDetailsDto {
    private Integer id;
    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("vote_average")
    private Float voteAverage;

    @JsonProperty("original_language")
    private String originalLanguage;

    private TmdbVideosDto videos;

    private java.util.List<TmdbGenreResponseDto.TmdbGenreDto> genres;

    @JsonProperty("production_countries")
    private java.util.List<TmdbProductionCountryDto> productionCountries;

    @Data
    public static class TmdbProductionCountryDto {
        @JsonProperty("iso_3166_1")
        private String iso31661;
        private String name;
    }
}