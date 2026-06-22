package com.example.pemomovie.dto;

import java.io.Serializable;

public class NotificationDto implements Serializable {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private Long relatedId;
    private String createdAt;

    public NotificationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getRead() { return isRead; }
    public void setRead(Boolean read) { isRead = read; }

    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    private Boolean isClaimed;
    public Boolean getIsClaimed() { return isClaimed; }
    public void setIsClaimed(Boolean claimed) { isClaimed = claimed; }
}
