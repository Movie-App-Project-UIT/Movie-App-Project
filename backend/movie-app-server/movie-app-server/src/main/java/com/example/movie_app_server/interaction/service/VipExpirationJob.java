package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VipExpirationJob {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final NotificationService notificationService;

    // Chạy ngầm mỗi ngày vào lúc 00:00 (12 giờ đêm)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void notifyExpiringVipUsers() {
        log.info("Bắt đầu tiến trình kiểm tra VIP sắp hết hạn...");

        // Tìm thời điểm chính xác 3 ngày tới
        LocalDateTime startOfDay = LocalDateTime.now().plusDays(3).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59);

        // Lấy danh sách các gói VIP đang Active và sẽ hết hạn vào 3 ngày nữa
        List<UserSubscription> expiringSubs = userSubscriptionRepository.findByStatusAndEndDateBetween(
                SubscriptionStatus.ACTIVE, startOfDay, endOfDay);

        if (expiringSubs.isEmpty()) {
            log.info("Không có gói VIP nào sắp hết hạn trong 3 ngày tới.");
            return;
        }

        List<User> usersToNotify = expiringSubs.stream()
                .map(UserSubscription::getUser)
                .toList();

        String title = "Gói VIP sắp hết hạn!";
        String message = "Gói Premium của bạn sẽ hết hạn sau 3 ngày nữa. Hãy gia hạn để tiếp tục trải nghiệm xem phim không giới hạn nhé!";

        notificationService.createNotificationsForUsers(usersToNotify, title, message, NotificationType.SUBSCRIPTION_EXPIRING);

        log.info("Đã gửi thông báo nhắc nhở gia hạn VIP cho {} người dùng.", usersToNotify.size());
    }
}
