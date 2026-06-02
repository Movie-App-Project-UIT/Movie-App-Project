package com.example.movie_app_server.media.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "age_ratings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AgeRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name; // Ví dụ: "T18", "T13"

    @Column(length = 200)
    private String description; // Tùy chọn thêm: "Dành cho người trên 18 tuổi"
}