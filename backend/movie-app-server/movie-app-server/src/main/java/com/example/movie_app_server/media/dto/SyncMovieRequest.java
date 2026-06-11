package com.example.movie_app_server.media.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SyncMovieRequest {
    @NotNull(message = "ID phim trên TMDB không được để trống")
    private Integer tmdbId;      // Bắt buộc: ID phim trên TMDB
    private String videoUrl;     // Không bắt buộc: Admin có thể để trống (null) lúc mới cào
    private boolean isPremium;   // Phim VIP (true) hay Miễn phí (false)?
}