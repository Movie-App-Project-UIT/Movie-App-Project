package com.example.movie_app_server.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminGenreDto {
    private Long id;
    private String name;
    private String colorCode;
    private int viewCount;
    private int mediaCount;
    @com.fasterxml.jackson.annotation.JsonProperty("isDeleted")
    private boolean isDeleted;
}
