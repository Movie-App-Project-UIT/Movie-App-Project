package com.example.movie_app_server.interaction.controller;

import com.example.movie_app_server.interaction.entity.Notification;
import com.example.movie_app_server.interaction.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final com.example.movie_app_server.user.repository.UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        String uid = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByFirebaseUid(uid)
                .map(user -> ResponseEntity.ok(notificationService.getUserNotifications(user.getId())))
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getMyUnreadCount() {
        String uid = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByFirebaseUid(uid)
                .map(user -> ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnreadNotifications(user.getId()))))
                .orElse(ResponseEntity.status(401).build());
    }

    // Lấy danh sách thông báo của User
    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    // Đếm số thông báo chưa đọc
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Đánh dấu 1 thông báo là đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đọc thông báo"));
    }
}
