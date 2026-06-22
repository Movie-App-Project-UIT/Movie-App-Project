package com.example.movie_app_server.interaction.entity;

import com.example.movie_app_server.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_reports", indexes = {
        @Index(name = "idx_review_report_review", columnList = "review_id"),
        @Index(name = "idx_review_report_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_report_user_review", columnNames = {"review_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp(source = org.hibernate.annotations.SourceType.VM)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
