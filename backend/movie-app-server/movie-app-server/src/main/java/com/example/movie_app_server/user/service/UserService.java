package com.example.movie_app_server.user.service;

import com.example.movie_app_server.user.dto.UserProfileDto;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.entity.enums.Role;
import com.example.movie_app_server.user.entity.enums.Tier;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý các nghiệp vụ liên quan đến tài khoản người dùng.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Hàm đồng bộ thông tin User từ Firebase xuống MySQL.
     * Logic: Tìm xem UID này đã có trong Database chưa.
     * - Nếu có rồi: Trả về thông tin User cũ.
     * - Nếu chưa có: Tạo mới 1 tài khoản (Mặc định Role=USER, Tier=FREE) và lưu vào DB.
     */
    @Transactional
    public UserProfileDto syncUser(String firebaseUid, String email, String username, String avatarUrl) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userRepository.save(User.builder()
                        .firebaseUid(firebaseUid)
                        .email(email)
                        .username(username)
                        .avatarUrl(avatarUrl)
                        .build()));
        return convertToProfileDto(user);
    }

    // Lấy toàn bộ thông tin cá nhân của User dựa vào Firebase UID
    public UserProfileDto getUserProfile(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return convertToProfileDto(user);
    }

    private UserProfileDto convertToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .tier(user.getTier())
                .build();
    }

    public boolean hasPremiumAccess(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(user -> user.getRole() == Role.ADMIN || user.getTier() == Tier.PREMIUM)
                .orElse(false);
    }
}