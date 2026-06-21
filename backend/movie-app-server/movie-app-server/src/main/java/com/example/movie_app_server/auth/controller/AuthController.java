package com.example.movie_app_server.auth.controller;

import com.example.movie_app_server.auth.service.OtpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private com.example.movie_app_server.user.repository.UserRepository userRepository;

    @Autowired
    private com.example.movie_app_server.interaction.service.NotificationService notificationService;

    @Autowired
    private com.example.movie_app_server.interaction.repository.UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository subscriptionPlanRepository;

    private Map<String, String> msg(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            // Check if user exists in Firebase
            FirebaseAuth.getInstance().getUserByEmail(email);
            // Send OTP
            otpService.sendForgotPasswordOtp(email);
            return ResponseEntity.ok(msg("Đã gửi mã xác nhận đến email!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(msg("Email không tồn tại trong hệ thống!"));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        if (otpService.verifyOtp(email, code)) {
            return ResponseEntity.ok(msg("Mã xác nhận chính xác!"));
        } else {
            return ResponseEntity.badRequest().body(msg("Mã xác nhận không đúng!"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        String newPassword = payload.get("newPassword");

        if (otpService.verifyOtp(email, code)) {
            try {
                UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUid())
                        .setPassword(newPassword);
                FirebaseAuth.getInstance().updateUser(request);
                otpService.clearOtp(email);
                return ResponseEntity.ok(msg("Đổi mật khẩu thành công!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(msg("Lỗi khi cập nhật mật khẩu!"));
            }
        } else {
            return ResponseEntity.badRequest().body(msg("Mã xác nhận không đúng hoặc đã hết hạn!"));
        }
    }

    @PostMapping("/register-send-otp")
    public ResponseEntity<Map<String, String>> registerSendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(msg("Email không được để trống!"));
        }
        try {
            // Check if user exists in Firebase. If it DOES NOT throw exception -> email is already used.
            FirebaseAuth.getInstance().getUserByEmail(email);
            return ResponseEntity.badRequest().body(msg("Email này đã được sử dụng!"));
        } catch (Exception e) {
            // User does not exist, safe to send OTP
            otpService.sendRegisterOtp(email);
            return ResponseEntity.ok(msg("Đã gửi mã xác nhận đăng ký đến email!"));
        }
    }

    @PostMapping("/register-with-otp")
    public ResponseEntity<Map<String, String>> registerWithOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String code = payload.get("code");
        String username = payload.get("username");

        if (email == null || password == null || code == null) {
            return ResponseEntity.badRequest().body(msg("Vui lòng điền đầy đủ thông tin!"));
        }

        if (otpService.verifyOtp(email, code)) {
            try {
                // 1. Create user in Firebase
                com.google.firebase.auth.UserRecord.CreateRequest request = new com.google.firebase.auth.UserRecord.CreateRequest()
                        .setEmail(email)
                        .setPassword(password);
                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

                // 2. Create user in local DB
                com.example.movie_app_server.user.entity.User newUser = com.example.movie_app_server.user.entity.User.builder()
                        .firebaseUid(userRecord.getUid())
                        .email(email)
                        .username(username)
                        .build();
                newUser = userRepository.save(newUser);

                // 3. Tạo thông báo chào mừng
                notificationService.createNotification(
                        newUser,
                        "Chào mừng đến với PemoMovie",
                        "Đăng ký tài khoản thành công. Chúc bạn xem phim vui vẻ!",
                        com.example.movie_app_server.interaction.entity.enums.NotificationType.SYSTEM
                );

                // 4. Tạo gói quà tặng Tân thủ (7 ngày)
                com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan plan = 
                    subscriptionPlanRepository.findAll().stream().findFirst().orElseGet(() -> {
                        com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan p = new com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan();
                        p.setName("Gói Premium 7 ngày");
                        p.setDescription("Gói xem phim không quảng cáo 7 ngày");
                        p.setPrice(java.math.BigDecimal.ZERO);
                        p.setDurationDays(7);
                        p.setIsActive(true);
                        return subscriptionPlanRepository.save(p);
                    });

                com.example.movie_app_server.interaction.entity.subscription.UserSubscription giftSub = 
                    com.example.movie_app_server.interaction.entity.subscription.UserSubscription.builder()
                        .user(newUser)
                        .plan(plan)
                        .status(com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus.PENDING_GIFT)
                        .startDate(java.time.LocalDateTime.now())
                        .endDate(java.time.LocalDateTime.now().plusDays(plan.getDurationDays()))
                        .build();
                giftSub = userSubscriptionRepository.save(giftSub);

                // Tạo thông báo nhận quà (đính kèm ID gói quà để app có thể kích hoạt)
                notificationService.createNotificationWithRelatedId(
                        newUser,
                        "Quà tặng tân thủ",
                        "Bạn nhận được 7 ngày Premium miễn phí. Nhấn để kích hoạt ngay!",
                        com.example.movie_app_server.interaction.entity.enums.NotificationType.GIFT_RECEIVED,
                        giftSub.getId()
                );

                // 5. Clear OTP
                otpService.clearOtp(email);

                return ResponseEntity.ok(msg("Đăng ký tài khoản thành công!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(msg("Lỗi khi tạo tài khoản: " + e.getMessage()));
            }
        } else {
            return ResponseEntity.badRequest().body(msg("Mã xác nhận không đúng hoặc đã hết hạn!"));
        }
    }
}
