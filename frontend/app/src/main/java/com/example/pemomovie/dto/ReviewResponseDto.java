package com.example.pemomovie.dto;

import java.util.List;

public class ReviewResponseDto {
    private Long id;
    private String content;
    private String createdAt;
    private UserSummaryDto user;
    private List<ReviewResponseDto> replies;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
    }

    public List<ReviewResponseDto> getReplies() {
        return replies;
    }

    public void setReplies(List<ReviewResponseDto> replies) {
        this.replies = replies;
    }
}
