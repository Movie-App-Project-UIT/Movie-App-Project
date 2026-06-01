package com.example.movie_app_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Khóa chính kép cho bảng media_genre (media_id, genre_id).
 * Dùng @Embeddable để nhúng vào entity MediaGenre qua @EmbeddedId.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MediaGenreId implements Serializable {

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "genre_id", nullable = false)
    private Integer genreId;
}
