package com.example.movie_app_server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "media_genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaGenre {

    @EmbeddedId
    private MediaGenreId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaId")
    @JoinColumn(name = "media_id")
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;

}
