package com.example.movie_app_server.interaction.dto;

import jakarta.validation.constraints.NotBlank;

// Lớp phụ để hứng dữ liệu JSON từ Frontend gửi lên
@lombok.Data
public class ReviewRequestDto {
    Long mediaId;
    Long episodeId;
    Long parentId;
    
    @NotBlank(message = "Nội dung bình luận không được để trống")
    String content;
}
