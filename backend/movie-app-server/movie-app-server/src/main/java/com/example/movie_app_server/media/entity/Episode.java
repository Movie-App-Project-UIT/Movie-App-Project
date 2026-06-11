package com.example.movie_app_server.media.entity;

import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.entity.WatchHistory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "episodes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_episode", columnNames = {"season_id", "episode_number"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    @JsonIgnore
    private Season season;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "still_path", length = 500)
    private String stillPath;

    private Integer duration;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtitle> subtitles = new ArrayList<>();
}