package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.entity.WatchHistory;
import com.example.movie_app_server.interaction.service.WatchHistoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    private String getUid() { return (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }

    @PostMapping("/update")
    public ResponseEntity<?> updateHistory(@RequestBody UpdateHistoryRequest request) {
        if (request.getMediaId() == null || request.getProgressSeconds() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu tham số bắt buộc (mediaId, progressSeconds)"));
        }
        
        try {
            com.example.movie_app_server.interaction.dto.WatchHistoryItemDto history = watchHistoryService.updateHistory(
                    getUid(),
                    request.getMediaId(),
                    request.getEpisodeId(),
                    request.getProgressSeconds(),
                    request.getTotalDurationSeconds()
            );
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<com.example.movie_app_server.interaction.dto.WatchHistoryItemDto>> getUserHistory() {
        return ResponseEntity.ok(watchHistoryService.getUserHistory(getUid()));
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<com.example.movie_app_server.interaction.dto.WatchHistoryItemDto> getHistoryByMediaId(@PathVariable Long mediaId) {
        return ResponseEntity.ok(watchHistoryService.getHistoryByMediaId(getUid(), mediaId));
    }

    @Data
    public static class UpdateHistoryRequest {
        private Long mediaId;
        private Long episodeId; // Có thể null nếu là phim lẻ
        private Integer progressSeconds;
        private Integer totalDurationSeconds; // Thời lượng thực tế của video
    }
}
