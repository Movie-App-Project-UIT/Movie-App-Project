package com.example.movie_app_server.interaction.dto;

import com.example.movie_app_server.user.dto.UserSummaryDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserSummaryDto user; // Chỉ chứa tên và avatar
    private List<ReviewResponseDto> replies; // Đệ quy để hiển thị các câu trả lời
}