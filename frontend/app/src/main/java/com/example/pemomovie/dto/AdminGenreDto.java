package com.example.pemomovie.dto;

public class AdminGenreDto {
    private Long id;
    private String name;
    private String colorCode;
    private int viewCount;
    private int mediaCount;
    @com.google.gson.annotations.SerializedName(value = "isDeleted", alternate = {"deleted"})
    private boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getMediaCount() { return mediaCount; }
    public void setMediaCount(int mediaCount) { this.mediaCount = mediaCount; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
