package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.user.dto.UserProfileDto;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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

    // API: GET /api/v1/users/me
    // Tác dụng: Lấy thông tin cá nhân của người đang đăng nhập (tên, avatar, gói cước VIP hay Free)
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        return ResponseEntity.ok(userService.getUserProfile(getCurrentUserUid()));
    }
}