package com.example.movie_app_server.admin.controller;

import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.dto.MediaDetailResponse;
import com.example.movie_app_server.admin.dto.AdminMovieSaveRequest;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.MediaService;
import com.example.movie_app_server.media.service.TmdbSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final TmdbSyncService tmdbSyncService;

    @GetMapping
    public ResponseEntity<List<MediaItemDto>> getAllMovies() {
        List<MediaItemDto> movies = mediaRepository.findAll().stream()
                .map(mediaService::convertToItemDto)
                .toList();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<MediaDetailResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaDetailAdmin(id));
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteMovie(@PathVariable Long id) {
        return mediaRepository.findById(id).map(media -> {
            media.setDeleted(!media.isDeleted()); // Toggle
            mediaRepository.save(media);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/preview-tmdb")
    public ResponseEntity<MediaDetailResponse> previewFromTmdb(@RequestParam Integer tmdbId) {
        Media media = tmdbSyncService.previewMovieFromTmdb(tmdbId);
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<MediaDetailResponse> createMovie(@RequestBody AdminMovieSaveRequest request) {
        Media media = tmdbSyncService.previewMovieFromTmdb(request.getTmdbId());
        media.setVideoUrl(request.getVideoUrl());
        media.setPremium(request.isPremium());
        media.setDeleted(request.isDeleted());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            media.setTitle(request.getTitle());
        }
        if (request.getOverview() != null && !request.getOverview().isEmpty()) {
            media.setOverview(request.getOverview());
        }
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            media.setLanguage(request.getLanguage());
        }
        media = mediaRepository.save(media);
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<MediaDetailResponse> updateMovie(@PathVariable Long id, @RequestBody AdminMovieSaveRequest request) {
        Media media = mediaRepository.findById(id).orElse(null);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.getTmdbId() != null && request.getTmdbId() > 0) {
            try {
                Media freshTmdb = tmdbSyncService.previewMovieFromTmdb(request.getTmdbId());
                media.setTmdbId(freshTmdb.getTmdbId());
                media.setPosterPath(freshTmdb.getPosterPath());
                media.setBackdropPath(freshTmdb.getBackdropPath());
                media.setVoteAverage(freshTmdb.getVoteAverage());
                media.setReleaseDate(freshTmdb.getReleaseDate());
                media.setCountry(freshTmdb.getCountry());
                media.setMediaType(freshTmdb.getMediaType());
                
                media.getGenres().clear();
                if (freshTmdb.getGenres() != null) {
                    media.getGenres().addAll(freshTmdb.getGenres());
                }

                media.getCredits().clear();
                if (freshTmdb.getCredits() != null) {
                    for (com.example.movie_app_server.media.entity.Credit c : freshTmdb.getCredits()) {
                        c.setMedia(media);
                        media.getCredits().add(c);
                    }
                }
            } catch (Exception e) {
                // Ignore TMDB fetch error during update if it fails, but ideally it works
            }
        }
        
        if (request.getVideoUrl() != null) {
            media.setVideoUrl(request.getVideoUrl());
        }
        media.setPremium(request.isPremium());
        media.setDeleted(request.isDeleted());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            media.setTitle(request.getTitle());
        }
        if (request.getOverview() != null && !request.getOverview().isEmpty()) {
            media.setOverview(request.getOverview());
        }
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            media.setLanguage(request.getLanguage());
        }
        media = mediaRepository.save(media);
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }
}
