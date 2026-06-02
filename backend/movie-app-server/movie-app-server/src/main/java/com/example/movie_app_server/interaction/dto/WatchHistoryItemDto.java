package com.example.movie_app_server.interaction.dto;

import com.example.movie_app_server.media.dto.EpisodeDto;
import com.example.movie_app_server.media.dto.MediaItemDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WatchHistoryItemDto {
    private Long id;
    private Integer progressSeconds; // Số giây đã xem (để vẽ thanh màu đỏ dưới ảnh)
    private LocalDateTime lastWatchedAt;
    private MediaItemDto media;

    // Nếu là phim bộ, trường này sẽ chứa thông tin tập đang xem dở
    // Nếu là phim lẻ, trường này = null
    private EpisodeDto episode;
}