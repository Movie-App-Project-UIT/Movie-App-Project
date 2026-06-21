package com.example.movie_app_server.user.service;

import com.example.movie_app_server.media.service.ImageKitService;
import com.example.movie_app_server.user.dto.UserProfileDto;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.entity.enums.Role;
import com.example.movie_app_server.user.entity.enums.Tier;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service xử lý các nghiệp vụ liên quan đến tài khoản người dùng.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageKitService imageKitService;
    private final com.example.movie_app_server.interaction.service.NotificationService notificationService;
    private final com.example.movie_app_server.interaction.repository.UserSubscriptionRepository userSubscriptionRepository;
    private final com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository subscriptionPlanRepository;
 
    /**
     * Hàm đồng bộ thông tin User từ Firebase xuống MySQL.
     * Logic: Tìm xem UID này đã có trong Database chưa.
     * - Nếu có rồi: Trả về thông tin User cũ.
     * - Nếu chưa có: Tạo mới 1 tài khoản (Mặc định Role=USER, Tier=FREE) và lưu vào DB.
     */
    @Transactional
    public UserProfileDto syncUser(String firebaseUid, String email, String username, String avatarUrl) {
        java.util.Optional<User> optionalUser = userRepository.findByFirebaseUid(firebaseUid);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!user.isActive()) {
                throw new org.springframework.security.access.AccessDeniedException("Tài khoản của bạn đã bị khóa.");
            }
            boolean isUpdated = false;
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                isUpdated = true;
            }
            if (avatarUrl != null) {
                String cleanAvatarUrl = avatarUrl.trim();
                if (cleanAvatarUrl.startsWith("\"") && cleanAvatarUrl.endsWith("\"")) {
                    cleanAvatarUrl = cleanAvatarUrl.substring(1, cleanAvatarUrl.length() - 1);
                }
                if (!cleanAvatarUrl.equals(user.getAvatarUrl())) {
                    user.setAvatarUrl(cleanAvatarUrl);
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                user = userRepository.save(user);
            }
            return convertToProfileDto(user);
        } else {
            String cleanAvatarUrl = avatarUrl;
            if (cleanAvatarUrl != null) {
                cleanAvatarUrl = cleanAvatarUrl.trim();
                if (cleanAvatarUrl.startsWith("\"") && cleanAvatarUrl.endsWith("\"")) {
                    cleanAvatarUrl = cleanAvatarUrl.substring(1, cleanAvatarUrl.length() - 1);
                }
            }
            User newUser = userRepository.save(User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email)
                    .username(username)
                    .avatarUrl(cleanAvatarUrl)
                    .isActive(true)
                    .role(com.example.movie_app_server.user.entity.enums.Role.USER)
                    .tier(com.example.movie_app_server.user.entity.enums.Tier.FREE)
                    .build());

            // 1. Tạo thông báo chào mừng
            notificationService.createNotification(
                    newUser,
                    "Chào mừng đến với CineApp",
                    "Đăng ký tài khoản thành công. Chúc bạn xem phim vui vẻ!",
                    com.example.movie_app_server.interaction.entity.enums.NotificationType.SYSTEM
            );

            return convertToProfileDto(newUser);
        }
    }

    // Lấy toàn bộ thông tin cá nhân của User dựa vào Firebase UID
    public UserProfileDto getUserProfile(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
                
        if (!user.isActive()) {
            throw new org.springframework.security.access.AccessDeniedException("Tài khoản của bạn đã bị khóa.");
        }
        
        return convertToProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(String firebaseUid, String newUsername) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        user.setUsername(newUsername);
        User saved = userRepository.save(user);
        return convertToProfileDto(saved);
    }

    private UserProfileDto convertToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .tier(user.getTier())
                .role(user.getRole())
                .build();
    }

    public boolean hasPremiumAccess(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(user -> user.getRole() == Role.ADMIN || user.getTier() == Tier.PREMIUM)
                .orElse(false);
    }

    @Transactional
    public String updateAvatar(String firebaseUid, MultipartFile file) throws Exception {
        // 1. Đẩy file vật lý lên ImageKit để lấy link siêu tốc
        String newAvatarUrl = imageKitService.uploadImage(file);

        // 2. Tìm tài khoản trong DB
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 3. Cập nhật đường link mới và lưu lại
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);

        return newAvatarUrl;
    }
}