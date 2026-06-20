package com.example.pemomovie.mockdata;

public class UserMock {
    private String id;
    private String name;
    private String email;
    private boolean isPremium;
    private String avatarUrl;

    public UserMock(String id, String name, String email, boolean isPremium, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isPremium = isPremium;
        this.avatarUrl = avatarUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isPremium() { return isPremium; }
    public String getAvatarUrl() { return avatarUrl; }
}
