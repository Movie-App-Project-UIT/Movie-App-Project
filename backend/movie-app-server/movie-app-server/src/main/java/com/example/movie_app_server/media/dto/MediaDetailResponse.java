package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MediaDetailResponse {
    private Long id;
    private String title;
    private String overview;
    private Float voteAverage;
    private String posterUrl;
    private String backdropUrl;

    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
}
