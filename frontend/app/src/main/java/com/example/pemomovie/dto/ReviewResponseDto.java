package com.example.pemomovie.dto;

import java.util.List;

public class ReviewResponseDto {
    private Long id;
    private String content;
    private String createdAt;
    private Long parentId;
    private String parentUsername;
    private long reportCount;
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentUsername() {
        return parentUsername;
    }

    public void setParentUsername(String parentUsername) {
        this.parentUsername = parentUsername;
    }

    public long getReportCount() {
        return reportCount;
    }

    public void setReportCount(long reportCount) {
        this.reportCount = reportCount;
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
