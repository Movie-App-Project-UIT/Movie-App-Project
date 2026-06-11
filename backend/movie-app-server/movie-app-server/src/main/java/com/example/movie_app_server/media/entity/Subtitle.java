package com.example.movie_app_server.media.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subtitles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Subtitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String language; // Ví dụ: "vi", "en", "Vietnamese"

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    @JsonIgnore
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    @JsonIgnore
    private Episode episode;
}
