package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.dto.ReviewRequestDto;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews") @RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    private String getUid() { return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }

    // API: GET /api/v1/reviews/media/{mediaId} -> Kéo danh sách bình luận về App để hiển thị
    @GetMapping("/media/{mediaId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long mediaId) {
        return ResponseEntity.ok(reviewService.getReviewsByMedia(mediaId));
    }

    // API: POST /api/v1/reviews -> Đăng bình luận mới
    @PostMapping
    public ResponseEntity<Review> postReview(@RequestBody ReviewRequestDto req) {
        return ResponseEntity.ok(reviewService.createReview(
                getUid(), req.getMediaId(), req.getEpisodeId(), req.getParentId(), req.getContent()));
    }
}