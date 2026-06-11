package com.example.movie_app_server.media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateVideoRequest {
    @NotBlank(message = "Đường link video mới không được để trống")
    private String videoUrl;     // Đường link video mới
    private boolean isPremium;   // Thay đổi trạng thái VIP nếu muốn
}