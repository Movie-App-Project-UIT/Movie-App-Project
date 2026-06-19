package com.example.pemomovie.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfileDto {
    private String id;
    private String email;
    @SerializedName("username")
    private String name;
    private String avatarUrl;
    private boolean isPremium;
    private String role;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
