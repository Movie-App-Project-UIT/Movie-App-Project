package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.interaction.entity.Notification;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.interaction.repository.NotificationRepository;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

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

    @Transactional
    public List<Notification> getUserNotifications(Long userId) {
        checkAndCreateExpiringNotification(userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            if (n.getType() == NotificationType.GIFT_RECEIVED && n.getRelatedId() != null) {
                userSubscriptionRepository.findById(n.getRelatedId()).ifPresent(sub -> {
                    n.setIsClaimed(sub.getStatus() != com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus.PENDING_GIFT);
                });
            }
        }
        return notifications;
    }

    private void checkAndCreateExpiringNotification(Long userId) {
        log.info("Checking expiring notification for user {}", userId);
        userRepository.findById(userId).ifPresent(user -> {
            userSubscriptionRepository.findFirstByUserAndStatusOrderByEndDateDesc(user, com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    java.time.LocalDateTime endDate = sub.getEndDate();
                    log.info("Found active subscription: {}, endDate: {}", sub.getId(), endDate);
                    if (endDate != null && endDate.isAfter(now)) {
                        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
                        log.info("Days left: {}", daysLeft);
                        if (daysLeft <= 3) {
                            boolean alreadyNotified = notificationRepository.existsByUserAndTypeAndRelatedId(user, NotificationType.SUBSCRIPTION_EXPIRING, sub.getId());
                            log.info("Already notified: {}", alreadyNotified);
                            if (!alreadyNotified) {
                                String formattedDate = endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                log.info("Saving expiring notification for sub {}", sub.getId());
                                Notification warning = Notification.builder()
                                        .user(user)
                                        .title("Premium sắp hết hạn")
                                        .message(String.format("Gói Premium của bạn sẽ hết hạn sau %d ngày (%s). Gia hạn ngay để tiếp tục trải nghiệm!", daysLeft == 0 ? 1 : daysLeft, formattedDate))
                                        .type(NotificationType.SUBSCRIPTION_EXPIRING)
                                        .relatedId(sub.getId())
                                        .isRead(false)
                                        .createdAt(now)
                                        .build();
                                notificationRepository.saveAndFlush(warning);
                            }
                        }
                    }
                });
        });
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

    @Transactional
    public void clearExpiringNotification(User user) {
        notificationRepository.deleteByUserIdAndType(user.getId(), NotificationType.SUBSCRIPTION_EXPIRING);
    }
}
