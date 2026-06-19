package com.example.movie_app_server.admin.controller;

import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @PutMapping("/{id}/toggle-hidden")
    public ResponseEntity<Void> toggleReviewHidden(@PathVariable Long id) {
        return reviewRepository.findById(id).map(review -> {
            review.setHidden(!review.isHidden()); // Toggle
            reviewRepository.save(review);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
