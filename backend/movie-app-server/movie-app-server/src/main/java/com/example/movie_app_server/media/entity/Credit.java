package com.example.movie_app_server.media.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "credits")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID của cá nhân đó trên TMDB (Ví dụ: 1190668)
    @Column(name = "tmdb_person_id", nullable = false)
    private Integer tmdbPersonId;

    // Tên thật của diễn viên/đạo diễn (Ví dụ: "Timothée Chalamet")
    @Column(nullable = false, length = 100)
    private String name;

    // Tên nhân vật trong phim (Ví dụ: "Paul Atreides")
    // Lưu ý: Đạo diễn sẽ không có tên nhân vật nên để trống cũng được
    @Column(name = "character_name", length = 100)
    private String characterName;

    // Ảnh đại diện của người đó
    @Column(name = "profile_path", length = 500)
    private String profilePath;

    // Vai trò/Bộ phận (Ví dụ: "Acting" -> Diễn viên, "Directing" -> Đạo diễn)
    @Column(length = 50)
    private String department;

    // Mối quan hệ: Nhiều Credit thuộc về 1 bộ Phim (Media)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    @JsonIgnore
    private Media media;
}