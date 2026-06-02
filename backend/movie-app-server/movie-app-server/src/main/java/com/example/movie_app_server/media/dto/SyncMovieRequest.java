package com.example.movie_app_server.media.dto;

import lombok.Data;

@Data
public class SyncMovieRequest {
    private Integer tmdbId;      // Bắt buộc: ID phim trên TMDB
    private String videoUrl;     // Không bắt buộc: Admin có thể để trống (null) lúc mới cào
    private boolean isPremium;   // Phim VIP (true) hay Miễn phí (false)?
}