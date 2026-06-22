package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.admin.dto.AdminDashboardStatsDto;
import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        // Top 3 highest rated movies (liked movies)
        List<MediaItemDto> topLiked = mediaRepository.findTop10ByIsDeletedFalseOrderByVoteAverageDesc()
                .stream()
                .limit(3)
                .map(mediaService::convertToItemDto)
                .collect(Collectors.toList());

        // Top 3 trending movies in the last 7 days
        List<MediaItemDto> topTrending = mediaRepository.findTrendingMedia(LocalDateTime.now().minusDays(7), PageRequest.of(0, 3))
                .stream()
                .map(mediaService::convertToItemDto)
                .collect(Collectors.toList());

        AdminDashboardStatsDto stats = AdminDashboardStatsDto.builder()
                .topLikedMovies(topLiked)
                .topTrendingMovies(topTrending)
                .build();

        return ResponseEntity.ok(stats);
    }
}
