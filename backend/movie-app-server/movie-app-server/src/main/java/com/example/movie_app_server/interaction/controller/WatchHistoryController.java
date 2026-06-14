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

    @PostMapping("/update")
    public ResponseEntity<?> updateHistory(@RequestBody UpdateHistoryRequest request) {
        if (request.getUserId() == null || request.getMediaId() == null || request.getProgressSeconds() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu tham số bắt buộc (userId, mediaId, progressSeconds)"));
        }
        
        try {
            WatchHistory history = watchHistoryService.updateHistory(
                    request.getUserId(),
                    request.getMediaId(),
                    request.getEpisodeId(),
                    request.getProgressSeconds()
            );
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserHistory(@PathVariable Long userId) {
        List<WatchHistory> historyList = watchHistoryService.getUserHistory(userId);
        return ResponseEntity.ok(historyList);
    }

    @Data
    public static class UpdateHistoryRequest {
        private Long userId; // Tạm thời dùng userId truyền từ App
        private Long mediaId;
        private Long episodeId; // Có thể null nếu là phim lẻ
        private Integer progressSeconds;
    }
}
