package com.example.movie_app_server.media.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class TmdbGenreResponseDto {
    private List<TmdbGenreDto> genres;

    @Data
    public static class TmdbGenreDto {
        private Integer id;
        private String name;
    }
}
