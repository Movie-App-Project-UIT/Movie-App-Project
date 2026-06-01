package com.example.movie_app_server.entity;

import com.example.movie_app_server.entity.enums.AgeRating;
import com.example.movie_app_server.entity.enums.MediaStatus;
import com.example.movie_app_server.entity.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 300)
    private String slug;

    @Column(name = "tmdb_id", unique = true)
    private Integer tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path", length = 500)
    private String posterPath;

    @Column(name = "backdrop_path", length = 500)
    private String backdropPath;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 5)
    private String language;

    @Column(length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false)
    @Builder.Default
    private AgeRating ageRating = AgeRating.PG;

    private Integer duration;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MediaStatus status = MediaStatus.UPCOMING;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "vote_average", nullable = false, precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal voteAverage = BigDecimal.ZERO;

    @Column(name = "vote_count", nullable = false)
    @Builder.Default
    private Long voteCount = 0L;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
