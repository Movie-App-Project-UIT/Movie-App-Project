package com.example.movie_app_server.interaction.dto;

// Lớp phụ để hứng dữ liệu JSON từ Frontend gửi lên
@lombok.Data
public class ReviewRequestDto {
    Long mediaId;
    Long episodeId;
    Long parentId;
    String content;
}
