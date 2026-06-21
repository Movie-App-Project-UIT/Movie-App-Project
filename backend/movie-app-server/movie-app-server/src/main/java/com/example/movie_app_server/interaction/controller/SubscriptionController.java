package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.entity.Notification;
import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.interaction.repository.NotificationRepository;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final NotificationRepository notificationRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    @PostMapping("/claim-gift/{notificationId}")
    public ResponseEntity<String> claimGift(@PathVariable Long notificationId) {
        String firebaseUid = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByFirebaseUid(firebaseUid).orElse(null);
        if (currentUser == null) return ResponseEntity.status(401).body("User not found");

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || !notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.notFound().build();
        }

        Long userSubscriptionId = notification.getRelatedId();
        if (userSubscriptionId == null) {
            return ResponseEntity.badRequest().body("Invalid gift notification");
        }

        UserSubscription giftSub = userSubscriptionRepository.findById(userSubscriptionId).orElse(null);
        if (giftSub == null || giftSub.getStatus() != SubscriptionStatus.PENDING_GIFT) {
            return ResponseEntity.badRequest().body("Gift already claimed or not found");
        }

        // Kích hoạt gói quà tặng
        UserSubscription activeSub = userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(currentUser, SubscriptionStatus.ACTIVE).orElse(null);

        LocalDateTime now = LocalDateTime.now();
        giftSub.setStatus(SubscriptionStatus.ACTIVE);
        giftSub.setStartDate(now);

        if (activeSub != null && activeSub.getEndDate().isAfter(now)) {
            // Cộng dồn
            giftSub.setEndDate(activeSub.getEndDate().plusDays(giftSub.getPlan().getDurationDays()));
            // Hủy gói cũ (nếu muốn) hoặc để nguyên
            // Để đơn giản, ta cho gói cũ hết hạn luôn (cập nhật endDate về hiện tại)
            activeSub.setEndDate(now);
            userSubscriptionRepository.save(activeSub);
        } else {
            giftSub.setEndDate(now.plusDays(giftSub.getPlan().getDurationDays()));
        }

        userSubscriptionRepository.save(giftSub);

        // Đánh dấu đã đọc
        notification.setIsRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok("Claimed successfully");
    }
}
