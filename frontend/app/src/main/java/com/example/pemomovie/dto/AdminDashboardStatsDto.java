package com.example.pemomovie.dto;

import java.util.List;

public class AdminDashboardStatsDto {
    private List<MediaItemDto> topLikedMovies;
    private List<MediaItemDto> topTrendingMovies;

    public List<MediaItemDto> getTopLikedMovies() {
        return topLikedMovies;
    }

    public void setTopLikedMovies(List<MediaItemDto> topLikedMovies) {
        this.topLikedMovies = topLikedMovies;
    }

    public List<MediaItemDto> getTopTrendingMovies() {
        return topTrendingMovies;
    }

    public void setTopTrendingMovies(List<MediaItemDto> topTrendingMovies) {
        this.topTrendingMovies = topTrendingMovies;
    }
}
