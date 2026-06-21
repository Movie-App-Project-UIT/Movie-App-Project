package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.admin.dto.AdminUserDetailDto;
import com.example.movie_app_server.interaction.dto.ReviewResponseDto;
import com.example.movie_app_server.interaction.entity.Review;
import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.interaction.repository.ReviewRepository;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import com.example.movie_app_server.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ReviewRepository reviewRepository;
    private final AdminHistoryService adminHistoryService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @RequestParam(required = false) Boolean isPremium,
            @RequestParam(required = false) String search) {
        if (isPremium == null && (search == null || search.trim().isEmpty())) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.ok(userRepository.searchAndFilterUsers(isPremium, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<AdminUserDetailDto> getUserDetails(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            AdminUserDetailDto.AdminUserDetailDtoBuilder builder = AdminUserDetailDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .avatarUrl(user.getAvatarUrl())
                    .role(user.getRole().name())
                    .tier(user.getTier().name())
                    .isActive(user.isActive())
                    .createdAt(user.getCreatedAt());

            UserSubscription activeSub = userSubscriptionRepository
                    .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE).orElse(null);
            
            if (activeSub != null && "PREMIUM".equals(user.getTier().name())) {
                builder.currentPlanName(activeSub.getPlan().getName());
                builder.planEndDate(activeSub.getEndDate());
            }

            List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            List<ReviewResponseDto> reviewDtos = reviews.stream().map(r -> ReviewResponseDto.builder()
                    .id(r.getId())
                    .content(r.getContent())
                    .createdAt(r.getCreatedAt())
                    .build()).collect(Collectors.toList());
            
            builder.reviews(reviewDtos);

            return ResponseEntity.ok(builder.build());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            boolean newStatus = !user.isActive();
            user.setActive(newStatus);
            userRepository.save(user);
            adminHistoryService.logAction(
                newStatus ? "RESTORE" : "DELETE",
                "USER",
                user.getId().toString(),
                (newStatus ? "Mở khóa tài khoản: " : "Khóa tài khoản: ") + user.getEmail()
            );
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
