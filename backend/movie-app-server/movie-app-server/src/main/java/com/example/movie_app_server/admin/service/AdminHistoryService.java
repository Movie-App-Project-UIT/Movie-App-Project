package com.example.movie_app_server.admin.service;

import com.example.movie_app_server.admin.entity.AdminHistory;
import com.example.movie_app_server.admin.repository.AdminHistoryRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHistoryService {

    private final AdminHistoryRepository adminHistoryRepository;
    private final UserRepository userRepository;

    public void logAction(String actionType, String entityType, String entityId, String details) {
        String uid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = "Unknown Admin";
        
        User admin = userRepository.findByFirebaseUid(uid).orElse(null);
        if (admin != null) {
            email = admin.getEmail();
        }

        AdminHistory history = AdminHistory.builder()
                .adminEmail(email)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        adminHistoryRepository.save(history);
    }

    public List<AdminHistory> getAllHistories() {
        return adminHistoryRepository.findAllByOrderByCreatedAtDesc();
    }
}
