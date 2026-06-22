package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.interaction.dto.ReviewResponseDto;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.repository.ReviewReportRepository;
import com.example.movie_app_server.interaction.repository.ReviewRepository;
import com.example.movie_app_server.interaction.service.ReviewService;
import com.example.movie_app_server.user.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewAdmin(@PathVariable Long id) {
        String adminUid = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.deleteReview(id, adminUid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/media/{mediaId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByMediaForAdmin(@PathVariable Long mediaId) {
        List<Review> reviews = reviewRepository.findAllByMediaIdWithUser(mediaId);
        List<ReviewResponseDto> dtos = reviews.stream()
                .filter(r -> r.getParent() == null)
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .map(r -> convertToAdminResponseDto(r))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private ReviewResponseDto convertToAdminResponseDto(Review review) {
        long reportCount = reviewReportRepository.countByReviewId(review.getId());
        return ReviewResponseDto.builder()
                .id(review.getId())
                .parentId(review.getParent() != null ? review.getParent().getId() : null)
                .parentUsername(review.getParent() != null ? review.getParent().getUser().getUsername() : null)
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .reportCount(reportCount)
                .user(UserSummaryDto.builder()
                        .username(review.getUser().getUsername())
                        .avatarUrl(review.getUser().getAvatarUrl())
                        .build())
                .replies(review.getReplies() != null ? 
                         review.getReplies().stream().map(r -> convertToAdminResponseDto(r)).toList() : null)
                .build();
    }
}
