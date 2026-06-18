package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.user.dto.UserProfileDto;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller xử lý các request liên quan đến User (Yêu cầu phải có Token đăng nhập).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Hàm tiện ích nội bộ: Lấy ID người dùng (UID) từ Token Firebase đã được Security Filter giải mã
    private String getCurrentUserUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // API: POST /api/v1/users/sync
    // Tác dụng: App Android gọi API này NGAY SAU KHI đăng nhập Firebase thành công để backend lưu data.
    @PostMapping("/sync")
    public ResponseEntity<UserProfileDto> syncUser(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(userService.syncUser(
                getCurrentUserUid(), payload.get("email"), payload.get("username"), payload.get("avatarUrl")));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        return ResponseEntity.ok(userService.getUserProfile(getCurrentUserUid()));
    }

    // API: PUT /api/v1/users/me/profile
    // Tác dụng: Cập nhật thông tin cá nhân (hiện tại là đổi username)
    @PutMapping("/me/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@RequestBody Map<String, String> payload) {
        String newUsername = payload.get("username");
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.updateProfile(getCurrentUserUid(), newUsername));
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        // Lấy mã Firebase UID của người đang gọi API
        String uid = getCurrentUserUid();

        // Cập nhật và lấy đường link ảnh mới trả về cho App Android hiển thị ngay lập tức
        String newAvatarUrl = userService.updateAvatar(uid, file);

        return ResponseEntity.ok(newAvatarUrl);
    }
}