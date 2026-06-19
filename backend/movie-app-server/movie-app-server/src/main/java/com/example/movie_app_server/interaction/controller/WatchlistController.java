package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.entity.Watchlist;
import com.example.movie_app_server.interaction.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watchlist") @RequiredArgsConstructor
public class WatchlistController {
    private final WatchlistService watchlistService;

    private String getUid() { return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }

    // API: GET /api/v1/watchlist -> Lấy danh sách phim đã lưu
    @GetMapping
    public ResponseEntity<List<com.example.movie_app_server.interaction.dto.WatchlistItemDto>> getWatchlist() {
        return ResponseEntity.ok(watchlistService.getMyWatchlist(getUid()));
    }

    // API: POST /api/v1/watchlist/{mediaId} -> Bấm nút để Thêm/Xóa phim khỏi list
    @PostMapping("/{mediaId}")
    public ResponseEntity<String> toggleWatchlist(@PathVariable Long mediaId) {
        watchlistService.toggleWatchlist(getUid(), mediaId);
        return ResponseEntity.ok("Đã cập nhật danh sách yêu thích");
    }
}