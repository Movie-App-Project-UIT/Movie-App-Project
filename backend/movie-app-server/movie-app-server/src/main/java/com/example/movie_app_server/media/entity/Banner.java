package com.example.movie_app_server.media.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners", indexes = {
        @Index(name = "idx_banner_media", columnList = "media_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    @JsonIgnore
    private Media media;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 255)
    private String title;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp(source = org.hibernate.annotations.SourceType.VM)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
