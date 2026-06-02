package com.example.movie_app_server.media.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbSeasonDetailsDto {
    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    private List<TmdbEpisodeDto> episodes;

    @Data
    public static class TmdbEpisodeDto {
        private String name;
        private String overview;

        @JsonProperty("episode_number")
        private Integer episodeNumber;

        @JsonProperty("still_path")
        private String stillPath; // Ảnh thumbnail của tập phim

        private Integer runtime; // Thời lượng tập phim
    }
}