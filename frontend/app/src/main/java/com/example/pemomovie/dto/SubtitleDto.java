package com.example.pemomovie.dto;

public class SubtitleDto {
    private Long id;
    private String language;
    private String fileUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
