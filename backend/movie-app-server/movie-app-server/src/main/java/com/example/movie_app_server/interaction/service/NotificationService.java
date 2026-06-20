package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.interaction.entity.Notification;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.interaction.repository.NotificationRepository;
import com.example.movie_app_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User user, String title, String message, NotificationType type) {
        createNotificationWithRelatedId(user, title, message, type, null);
    }

    @Transactional
    public void createNotificationWithRelatedId(User user, String title, String message, NotificationType type, Long relatedId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationsForUsers(List<User> users, String title, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .isRead(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
}
