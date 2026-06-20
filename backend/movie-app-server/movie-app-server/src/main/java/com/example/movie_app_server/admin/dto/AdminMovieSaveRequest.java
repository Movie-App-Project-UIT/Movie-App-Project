package com.example.movie_app_server.admin.dto;

import lombok.Data;

@Data
public class AdminMovieSaveRequest {
    private Integer tmdbId;
    private String videoUrl;
    @com.fasterxml.jackson.annotation.JsonProperty("isPremium")
    private boolean isPremium;
    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;
    private String title;
    private String overview;
    private String language;
}
