package com.example.movie_app_server.media.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubtitleDto {
    private Long id;
    private String language;
    private String fileUrl;
}
