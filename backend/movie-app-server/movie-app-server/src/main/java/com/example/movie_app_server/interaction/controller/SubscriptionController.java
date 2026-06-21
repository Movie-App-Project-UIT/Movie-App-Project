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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<java.util.Map<String, String>> claimGift(@PathVariable Long notificationId) {
        String firebaseUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByFirebaseUid(firebaseUid).orElse(null);
        if (currentUser == null) return ResponseEntity.status(401).body(java.util.Map.of("message", "User not found"));

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null || !notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.notFound().build();
        }

        Long userSubscriptionId = notification.getRelatedId();
        if (userSubscriptionId == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid gift notification"));
        }

        UserSubscription giftSub = userSubscriptionRepository.findById(userSubscriptionId).orElse(null);
        if (giftSub == null || giftSub.getStatus() != SubscriptionStatus.PENDING_GIFT) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Gift already claimed or not found"));
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

        // Cập nhật tài khoản người dùng thành PREMIUM thực tế trong Database
        currentUser.setTier(com.example.movie_app_server.user.entity.enums.Tier.PREMIUM);
        userRepository.save(currentUser);

        // Đánh dấu đã đọc
        notification.setIsRead(true);
        notificationRepository.save(notification);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = giftSub.getEndDate().format(formatter);

        return ResponseEntity.ok(java.util.Map.of("message", "Kích hoạt thành công! Hạn mới: " + formattedDate));
    }
}
