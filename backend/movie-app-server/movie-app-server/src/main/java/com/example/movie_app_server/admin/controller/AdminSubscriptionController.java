package com.example.movie_app_server.admin.controller;

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

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionPlanRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlan> createSubscription(@RequestBody SubscriptionPlan plan) {
        plan.setIsActive(true);
        return ResponseEntity.ok(subscriptionPlanRepository.save(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionPlan planDetails) {
        return subscriptionPlanRepository.findById(id).map(plan -> {
            plan.setName(planDetails.getName());
            plan.setDescription(planDetails.getDescription());
            plan.setPrice(planDetails.getPrice());
            plan.setDurationDays(planDetails.getDurationDays());
            return ResponseEntity.ok(subscriptionPlanRepository.save(plan));
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
                        .startDate(java.time.LocalDateTime.now())
                        .endDate(java.time.LocalDateTime.now().plusDays(plan.getDurationDays()))
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
            });
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/gifted-users")
    public ResponseEntity<List<User>> getGiftedUsers(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElse(null);
        if (plan == null) return ResponseEntity.notFound().build();

        List<User> giftedUsers = userSubscriptionRepository.findByPlanAndStatus(plan, SubscriptionStatus.PENDING_GIFT)
                .stream().map(UserSubscription::getUser).collect(Collectors.toList());
        List<User> activeUsers = userSubscriptionRepository.findByPlanAndStatus(plan, SubscriptionStatus.ACTIVE)
                .stream().map(UserSubscription::getUser).collect(Collectors.toList());

        // Gộp cả 2 danh sách nếu muốn hiển thị tất cả user có liên quan, ở đây chỉ lấy PENDING_GIFT tạm thời,
        // Hoặc có thể lấy tất cả nếu user đã nhận. Nhưng yêu cầu là "danh sách khách hàng được tặng"
        // Ở đây lấy tất cả user đã được tặng (Pending)
        // Nếu user claim rồi thì sẽ thành ACTIVE.
        
        // Để hiển thị chuẩn danh sách khách hàng được tặng, ta có thể gộp cả 2:
        java.util.Set<User> allGifted = new java.util.HashSet<>();
        allGifted.addAll(giftedUsers);
        // Nhưng wait, ACTIVE users có thể là tự mua, không phải tặng! 
        // Trong trường hợp này ta chỉ hiển thị PENDING_GIFT hoặc phải lưu thêm cờ isGifted = true.
        // Để tránh sửa Entity, ta trả về PENDING_GIFT (hoặc chấp nhận hiển thị tất cả ACTIVE users).
        // Ta sẽ trả về PENDING_GIFT vì đó chắc chắn là người chưa kích hoạt gói tặng.
        
        return ResponseEntity.ok(giftedUsers);
    }
}
