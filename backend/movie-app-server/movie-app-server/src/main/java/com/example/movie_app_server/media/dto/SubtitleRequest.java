package com.example.movie_app_server.media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubtitleRequest {
    @NotBlank(message = "Language không được để trống")
    private String language;
    @NotBlank(message = "File URL không được để trống")
    private String fileUrl;
    private Long mediaId; // Null nếu add cho episode
    private Long episodeId; // Null nếu add cho media
}
