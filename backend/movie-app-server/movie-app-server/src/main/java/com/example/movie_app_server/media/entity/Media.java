package com.example.movie_app_server.media.entity;

import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.entity.WatchHistory;
import com.example.movie_app_server.interaction.entity.Watchlist;
import com.example.movie_app_server.media.entity.enums.MediaType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_media_type", columnList = "media_type"),
        @Index(name = "idx_tmdb_id", columnList = "tmdb_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Integer tmdbId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "poster_path", length = 500)
    private String posterPath;

    @Column(name = "backdrop_path", length = 500)
    private String backdropPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "vote_average")
    private Float voteAverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "duration")
    private Integer duration;

    // --- MỐI QUAN HỆ DANH MỤC ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_rating_id")
    private AgeRating ageRating;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "media_genre",
            joinColumns = @JoinColumn(name = "media_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private boolean isPremium = false;

    // --- MỐI QUAN HỆ CON ---

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Watchlist> watchlists = new ArrayList<>();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Credit> credits = new ArrayList<>();

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtitle> subtitles = new ArrayList<>();
}