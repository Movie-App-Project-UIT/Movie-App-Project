package com.example.movie_app_server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history", indexes = {
        @Index(name = "idx_wh_user", columnList = "user_id"),
        @Index(name = "idx_wh_episode", columnList = "episode_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    private Episode episode;

    @Column(name = "progress_seconds", nullable = false)
    @Builder.Default
    private Integer progressSeconds = 0;

    @UpdateTimestamp
    @Column(name = "last_watched_at", nullable = false)
    private LocalDateTime lastWatchedAt;
}
