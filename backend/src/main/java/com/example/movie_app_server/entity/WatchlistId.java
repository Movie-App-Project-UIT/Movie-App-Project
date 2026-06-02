package com.example.movie_app_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Khóa chính kép cho bảng watchlist (user_id, media_id).
 * Dùng @Embeddable để nhúng vào entity Watchlist qua @EmbeddedId.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WatchlistId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;
}
