package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific user, ordered by newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Count how many unread notifications the user has
    long countByUserIdAndIsReadFalse(Long userId);
}
