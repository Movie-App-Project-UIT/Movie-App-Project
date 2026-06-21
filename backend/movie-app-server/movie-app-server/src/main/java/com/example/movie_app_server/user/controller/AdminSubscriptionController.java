package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.admin.dto.GiftSubscriptionRequest;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan;
import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.interaction.service.NotificationService;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import com.example.movie_app_server.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AdminHistoryService adminHistoryService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionPlanRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlan> createSubscription(@RequestBody SubscriptionPlan plan) {
        plan.setIsActive(true);
        SubscriptionPlan saved = subscriptionPlanRepository.save(plan);
        adminHistoryService.logAction("CREATE", "SUBSCRIPTION", saved.getId().toString(), "Thêm gói Premium mới: " + saved.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionPlan planDetails) {
        return subscriptionPlanRepository.findById(id).map(plan -> {
            plan.setName(planDetails.getName());
            plan.setDescription(planDetails.getDescription());
            plan.setPrice(planDetails.getPrice());
            plan.setDurationDays(planDetails.getDurationDays());
            SubscriptionPlan saved = subscriptionPlanRepository.save(plan);
            adminHistoryService.logAction("UPDATE", "SUBSCRIPTION", saved.getId().toString(), "Cập nhật thông tin gói: " + saved.getName());
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleSubscriptionStatus(@PathVariable Long id) {
        return subscriptionPlanRepository.findById(id).map(plan -> {
            boolean newStatus = !plan.getIsActive();
            plan.setIsActive(newStatus);
            subscriptionPlanRepository.save(plan);

            // Nếu ẩn gói, thông báo cho user đang sử dụng
            if (!newStatus) {
                List<UserSubscription> activeSubs = userSubscriptionRepository.findByPlanAndStatus(plan, SubscriptionStatus.ACTIVE);
                for (UserSubscription sub : activeSubs) {
                    notificationService.createNotification(
                            sub.getUser(),
                            "Gói " + plan.getName() + " đã ngừng đăng ký mới",
                            "Bạn vẫn có thể tiếp tục sử dụng các đặc quyền của gói này cho đến khi hết hạn.",
                            NotificationType.SUBSCRIPTION_HIDDEN
                    );
                }
            }

            adminHistoryService.logAction(
                newStatus ? "RESTORE" : "DELETE",
                "SUBSCRIPTION",
                plan.getId().toString(),
                (newStatus ? "Mở bán lại gói: " : "Ngừng bán gói: ") + plan.getName()
            );

            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/eligible-users")
    public ResponseEntity<List<User>> getEligibleUsers(@RequestParam(required = false) Boolean isPremium,
                                                       @RequestParam(required = false) String search) {
        // Find users with filters
        return ResponseEntity.ok(userRepository.searchAndFilterUsers(isPremium, search));
    }

    @PostMapping("/{id}/gift")
    public ResponseEntity<Void> giftSubscription(@PathVariable Long id, @RequestBody GiftSubscriptionRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();

        for (Long userId : request.getUserIds()) {
            userRepository.findById(userId).ifPresent(user -> {
                // Tạo UserSubscription trạng thái PENDING_GIFT
                UserSubscription userSubscription = UserSubscription.builder()
                        .user(user)
                        .plan(plan)
                        .status(SubscriptionStatus.PENDING_GIFT)
                        .isGift(true)
                        .build();
                UserSubscription savedSub = userSubscriptionRepository.save(userSubscription);

                // Gửi thông báo tặng quà với relatedId = UserSubscription.id
                notificationService.createNotificationWithRelatedId(
                        user,
                        "Bạn nhận được quà tặng!",
                        "Bạn vừa được tặng gói " + plan.getName() + ". Vui lòng xác nhận để kích hoạt.",
                        NotificationType.GIFT_RECEIVED,
                        savedSub.getId()
                );
                adminHistoryService.logAction("GIFT", "SUBSCRIPTION", plan.getId().toString(), "Tặng gói '" + plan.getName() + "' cho user: " + user.getEmail());
            });
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/gifted-users")
    public ResponseEntity<List<User>> getGiftedUsers(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();

        // Lấy tất cả user đã được nhận gói quà (Dù đang PENDING_GIFT hay đã ACTIVE)
        List<User> giftedUsers = userSubscriptionRepository.findByPlanAndIsGift(plan, true)
                .stream().map(UserSubscription::getUser).distinct().collect(Collectors.toList());
        
        return ResponseEntity.ok(giftedUsers);
    }
}
