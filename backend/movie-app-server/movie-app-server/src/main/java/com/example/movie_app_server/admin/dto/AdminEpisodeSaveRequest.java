package com.example.movie_app_server.admin.dto;

import lombok.Data;

@Data
public class AdminEpisodeSaveRequest {
    private String videoUrl;
    @com.fasterxml.jackson.annotation.JsonProperty("isPremium")
    private boolean isPremium;
    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;
    private String title;
    private String overview;
    private java.util.List<AdminMovieSaveRequest.AdminSubtitleRequest> subtitles;
}
