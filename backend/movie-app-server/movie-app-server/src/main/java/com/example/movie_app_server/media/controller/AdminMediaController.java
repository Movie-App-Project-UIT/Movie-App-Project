package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final TmdbService tmdbService;

    public AdminMediaController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @PostMapping("/fetch-tmdb")
    public ResponseEntity<Media> fetchFromTmdb(@RequestParam Integer tmdbId) {
        try {
            Media media = tmdbService.fetchAndSaveMovie(tmdbId);
            return ResponseEntity.ok(media);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
