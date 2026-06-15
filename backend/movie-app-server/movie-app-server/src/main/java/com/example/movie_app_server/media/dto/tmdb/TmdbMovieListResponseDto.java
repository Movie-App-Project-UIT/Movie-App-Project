package com.example.movie_app_server.media.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieListResponseDto {
    private List<TmdbMovieBasicDto> results;

    @Data
    public static class TmdbMovieBasicDto {
        private Integer id;
        private String title;
    }
}
