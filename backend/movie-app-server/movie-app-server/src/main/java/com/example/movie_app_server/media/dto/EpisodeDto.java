package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EpisodeDto {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String overview;
    private String stillUrl;
    private Integer duration;
    private boolean isPlayable;
    private java.util.List<SubtitleDto> subtitles;
}