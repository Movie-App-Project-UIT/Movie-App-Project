package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.Notification;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a specific user, ordered by newest first
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Count how many unread notifications the user has
    long countByUserIdAndIsReadFalse(Long userId);

    boolean existsByUserAndTypeAndRelatedId(
        User user, 
        NotificationType type, 
        Long relatedId
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId AND n.type = :type")
    void deleteByUserIdAndType(
        @Param("userId") Long userId, 
        @Param("type") NotificationType type
    );
}
