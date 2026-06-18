package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.dto.EpisodeDto;
import com.example.movie_app_server.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cung cấp API để App Android lấy danh sách và chi tiết các Tập (Episodes) phim.
 */
@RestController
@RequestMapping("/api/v1/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final MediaService mediaService;

    // API: GET /api/v1/episodes?seasonId=1 -> Lấy danh sách tập của một phần phim
    @GetMapping
    public ResponseEntity<List<EpisodeDto>> getEpisodesBySeason(@RequestParam Long seasonId) {
        return ResponseEntity.ok(mediaService.getEpisodesBySeasonId(seasonId));
    }

    // API: GET /api/v1/episodes/{id} -> Lấy chi tiết 1 tập phim (thông tin + link video + phụ đề)
    @GetMapping("/{id}")
    public ResponseEntity<EpisodeDto> getEpisodeById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getEpisodeById(id));
    }
}
