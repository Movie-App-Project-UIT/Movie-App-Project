package com.example.movie_app_server.user.dto;

import com.example.movie_app_server.user.entity.enums.Tier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Tier tier;
    private com.example.movie_app_server.user.entity.enums.Role role;
    
    private int watchedMoviesCount;
    private int watchedHours;
    private int streakDays;
}