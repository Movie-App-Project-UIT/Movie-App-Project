package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.entity.AgeRating;
import com.example.movie_app_server.media.entity.Country;
import com.example.movie_app_server.media.entity.Genre;
import com.example.movie_app_server.media.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller cung cấp các API (Endpoints) mở (không cần đăng nhập)
 * để App Android kéo dữ liệu danh mục về máy.
 */
@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
public class LookupController {
    private final LookupService lookupService;

    // API: GET /api/v1/lookups/countries -> Trả về JSON danh sách quốc gia
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getCountries() {
        return ResponseEntity.ok(lookupService.getAllCountries());
    }

    // API: GET /api/v1/lookups/age-ratings -> Trả về JSON danh sách độ tuổi
    @GetMapping("/age-ratings")
    public ResponseEntity<List<AgeRating>> getAgeRatings() {
        return ResponseEntity.ok(lookupService.getAllAgeRatings());
    }

    // API: GET /api/v1/lookups/genres -> Trả về JSON danh sách thể loại phim
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getGenres() {
        return ResponseEntity.ok(lookupService.getAllGenres());
    }
}