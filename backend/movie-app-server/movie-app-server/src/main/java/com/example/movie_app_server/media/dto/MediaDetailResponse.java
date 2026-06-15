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
    private Integer releaseYear;
    private Integer duration;
    private Integer viewCount;
    private boolean isPremium;
    private String mediaType;
    private String genre;
    private String country;
    private String language;

    private List<CreditDto> directors;
    private List<CreditDto> cast;
    private List<SubtitleDto> subtitles;
}
