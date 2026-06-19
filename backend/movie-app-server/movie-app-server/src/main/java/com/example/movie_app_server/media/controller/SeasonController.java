package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.dto.SeasonDto;
import com.example.movie_app_server.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cung cấp API để App Android lấy danh sách các Phần (Seasons) của phim bộ.
 */
@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final MediaService mediaService;

    // API: GET /api/v1/seasons?mediaId=1 -> Lấy danh sách các phần (kèm tập) của một phim bộ
    @GetMapping
    public ResponseEntity<List<SeasonDto>> getSeasonsByMedia(@RequestParam Long mediaId) {
        return ResponseEntity.ok(mediaService.getSeasonsByMediaId(mediaId));
    }

    // API: GET /api/v1/seasons/{id} -> Lấy chi tiết 1 phần phim (kèm danh sách tập)
    @GetMapping("/{id}")
    public ResponseEntity<SeasonDto> getSeasonById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getSeasonById(id));
    }
}
