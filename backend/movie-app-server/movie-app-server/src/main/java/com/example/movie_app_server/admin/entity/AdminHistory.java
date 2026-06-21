package com.example.movie_app_server.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email của admin thực hiện
    @Column(nullable = false)
    private String adminEmail;

    // Loại hành động: CREATE, UPDATE, DELETE, TOGGLE_STATUS, GIFT, v.v.
    @Column(nullable = false)
    private String actionType;

    // Loại đối tượng: MOVIE, CATEGORY, SUBSCRIPTION, USER, v.v.
    @Column(nullable = false)
    private String entityType;

    // ID của đối tượng bị tác động (có thể là String hoặc Long, nên để String cho linh hoạt)
    @Column(nullable = false)
    private String entityId;

    // Nội dung chi tiết
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
