package com.example.pemomovie.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {
    private String id;
    private String email;
    @SerializedName("username")
    private String name;
    private String avatarUrl;
    private String tier;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    
    public boolean isPremium() {
        return "PREMIUM".equalsIgnoreCase(this.tier);
    }
}
