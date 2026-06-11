package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.dto.SyncMovieRequest;
import com.example.movie_app_server.media.dto.UpdateEpisodeVideoRequest;
import com.example.movie_app_server.media.dto.UpdateVideoRequest;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.TmdbSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
public class AdminSyncController {

    private final TmdbSyncService syncService;
    private final MediaRepository mediaRepository;
    private final EpisodeRepository episodeRepository;

    @PostMapping("/movie")
    public ResponseEntity<String> syncMovie(@Valid @RequestBody SyncMovieRequest request) {
        // UPDATED: Used the instance variable 'syncService' instead of the class name
        Media savedMedia = syncService.syncMovieFromTmdb(
                request.getTmdbId(),
                request.getVideoUrl(),
                request.isPremium()
        );
        return ResponseEntity.ok(
                "Thành công! Đã đồng bộ phim: " + savedMedia.getTitle() +
                        " (VIP: " + savedMedia.isPremium() + ")" +
                        " cùng với " + savedMedia.getCredits().size() + " đạo diễn/diễn viên."
        );
    }

    @PutMapping("/movie/{mediaId}/video")
    public ResponseEntity<String> updateMovieVideo(
            @PathVariable Long mediaId,
            @Valid @RequestBody UpdateVideoRequest request) {

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim"));

        media.setVideoUrl(request.getVideoUrl());
        media.setPremium(request.isPremium());
        mediaRepository.save(media);

        return ResponseEntity.ok("Cập nhật video thành công cho phim: " + media.getTitle());
    }

    @PostMapping("/tv")
    public ResponseEntity<String> syncTvSeries(@Valid @RequestBody SyncMovieRequest request) {
        Media savedMedia = syncService.syncTvSeriesFromTmdb(request.getTmdbId());
        return ResponseEntity.ok("Đã đồng bộ Phim Bộ: " + savedMedia.getTitle() + " cùng toàn bộ các Phần và Tập!");
    }

    @PutMapping("/episode/{episodeId}/video")
    public ResponseEntity<String> updateEpisodeVideo(
            @PathVariable Long episodeId,
            @Valid @RequestBody UpdateEpisodeVideoRequest request) {

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tập phim"));

        episode.setVideoUrl(request.getVideoUrl());
        episodeRepository.save(episode);

        return ResponseEntity.ok("Đã cập nhật video cho Tập " + episode.getEpisodeNumber());
    }
}