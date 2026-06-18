package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SeasonDto {
    private Long id;
    private Integer seasonNumber;
    private String title;
    private String overview;
    private String posterUrl;
    private List<EpisodeDto> episodes; // Chứa danh sách các tập bên trong
}