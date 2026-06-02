package com.example.movie_app_server.interaction.dto;

import com.example.movie_app_server.media.dto.MediaItemDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WatchlistItemDto {
    private Long id;
    private LocalDateTime addedAt;
    private MediaItemDto media; // Sử dụng MediaItemDto để lấy ảnh và tên phim
}