package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.dto.ReviewRequestDto;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews") @RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    private String getUid() { return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }

    // API: GET /api/v1/reviews/media/{mediaId} -> Kéo danh sách bình luận về App để hiển thị
    @GetMapping("/media/{mediaId}")
    public ResponseEntity<List<com.example.movie_app_server.interaction.dto.ReviewResponseDto>> getReviews(@PathVariable Long mediaId) {
        return ResponseEntity.ok(reviewService.getReviewsByMedia(mediaId));
    }

    // API: POST /api/v1/reviews -> Đăng bình luận mới
    @PostMapping
    public ResponseEntity<com.example.movie_app_server.interaction.dto.ReviewResponseDto> postReview(@Valid @RequestBody ReviewRequestDto req) {
        return ResponseEntity.ok(reviewService.createReview(
                getUid(), req.getMediaId(), req.getEpisodeId(), req.getParentId(), req.getContent()));
    }

    // API: PUT /api/v1/reviews/{id} -> Sửa bình luận
    @PutMapping("/{id}")
    public ResponseEntity<com.example.movie_app_server.interaction.dto.ReviewResponseDto> updateReview(
            @PathVariable Long id, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reviewService.updateReview(id, getUid(), content));
    }

    // API: DELETE /api/v1/reviews/{id} -> Xóa bình luận
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id, getUid());
        return ResponseEntity.ok().build();
    }
}