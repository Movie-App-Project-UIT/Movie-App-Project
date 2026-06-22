package com.example.movie_app_server.interaction.entity;

import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist", uniqueConstraints = {
        @UniqueConstraint(name = "uq_watchlist", columnNames = {"user_id", "media_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Watchlist {
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

    @CreationTimestamp(source = org.hibernate.annotations.SourceType.VM)
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}