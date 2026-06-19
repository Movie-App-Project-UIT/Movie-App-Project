package com.example.movie_app_server.admin.controller;

import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.MediaService;
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

    @GetMapping
    public ResponseEntity<List<MediaItemDto>> getAllMovies() {
        List<MediaItemDto> movies = mediaRepository.findAll().stream()
                .map(mediaService::convertToItemDto)
                .toList();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMovieById(@PathVariable Long id) {
        return mediaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteMovie(@PathVariable Long id) {
        return mediaRepository.findById(id).map(media -> {
            media.setDeleted(!media.isDeleted()); // Toggle
            mediaRepository.save(media);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
