package com.example.movie_app_server.admin.dto;

import com.example.movie_app_server.media.dto.MediaItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDto {
    private List<MediaItemDto> topLikedMovies;
    private List<MediaItemDto> topTrendingMovies;
}
