package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    long countByReviewId(Long reviewId);
    java.util.List<ReviewReport> findByUserId(Long userId);
}
