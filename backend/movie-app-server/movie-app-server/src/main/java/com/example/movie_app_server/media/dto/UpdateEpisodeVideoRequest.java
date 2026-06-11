package com.example.movie_app_server.media.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEpisodeVideoRequest {
    @NotBlank(message = "Đường link video không được để trống")
    private String videoUrl;
}