package com.example.movie_app_server.admin.controller;

import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final GenreRepository genreRepository;

    @GetMapping
    public ResponseEntity<List<Genre>> getAllCategories() {
        return ResponseEntity.ok(genreRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getCategoryById(@PathVariable Long id) {
        return genreRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteCategory(@PathVariable Long id) {
        return genreRepository.findById(id).map(genre -> {
            genre.setDeleted(!genre.isDeleted()); // Toggle
            genreRepository.save(genre);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
