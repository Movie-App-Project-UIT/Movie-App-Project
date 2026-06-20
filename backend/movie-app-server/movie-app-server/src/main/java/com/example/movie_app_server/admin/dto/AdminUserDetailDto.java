package com.example.movie_app_server.admin.dto;

import com.example.movie_app_server.interaction.dto.ReviewResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminUserDetailDto {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private String tier;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Premium Info
    private String currentPlanName;
    private LocalDateTime planEndDate;
    
    // Reviews
    private List<ReviewResponseDto> reviews;
}
