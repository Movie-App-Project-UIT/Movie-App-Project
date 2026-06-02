package com.example.movie_app_server.media.dto;

import lombok.Data;

@Data
public class UpdateVideoRequest {
    private String videoUrl;     // Đường link video mới
    private boolean isPremium;   // Thay đổi trạng thái VIP nếu muốn
}