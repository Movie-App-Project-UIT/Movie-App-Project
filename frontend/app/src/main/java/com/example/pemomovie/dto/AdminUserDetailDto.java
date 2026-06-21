package com.example.pemomovie.dto;

import java.util.List;

public class AdminUserDetailDto {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private String tier;
    
    @com.google.gson.annotations.SerializedName("active")
    private boolean isActive;
    
    private String createdAt;
    
    // Premium Info
    private String currentPlanName;
    private String planEndDate;
    
    // Reviews
    private List<ReviewDto> reviews;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public String getTier() { return tier; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public String getCurrentPlanName() { return currentPlanName; }
    public String getPlanEndDate() { return planEndDate; }
    public List<ReviewDto> getReviews() { return reviews; }

    public static class ReviewDto {
        private Long id;
        private String content;
        private String createdAt;
        private Long parentId;
        private long reportCount;

        public Long getId() { return id; }
        public String getContent() { return content; }
        public String getCreatedAt() { return createdAt; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public long getReportCount() { return reportCount; }
        public void setReportCount(long reportCount) { this.reportCount = reportCount; }
    }
}
