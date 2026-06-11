package com.example.movie_app_server.media.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class TmdbVideosDto {
    private List<TmdbVideoDto> results;

    @Data
    public static class TmdbVideoDto {
        private String key;
        private String type;
        private String site;
    }
}
