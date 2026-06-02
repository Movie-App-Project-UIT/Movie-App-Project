package com.example.movie_app_server.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryDto {
    private String username;
    private String avatarUrl;
}