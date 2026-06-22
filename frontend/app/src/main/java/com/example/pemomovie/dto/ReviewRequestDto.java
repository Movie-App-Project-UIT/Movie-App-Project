package com.example.pemomovie.dto;

public class ReviewRequestDto {
    private Long mediaId;
    private Long episodeId;
    private Long parentId;
    private String content;

    public ReviewRequestDto(Long mediaId, Long episodeId, Long parentId, String content) {
        this.mediaId = mediaId;
        this.episodeId = episodeId;
        this.parentId = parentId;
        this.content = content;
    }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
