package com.example.pemomovie.dto;

public class UpdateHistoryRequest {
    private Long mediaId;
    private Long episodeId;
    private Integer progressSeconds;

    public UpdateHistoryRequest(Long mediaId, Long episodeId, Integer progressSeconds) {
        this.mediaId = mediaId;
        this.episodeId = episodeId;
        this.progressSeconds = progressSeconds;
    }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public Integer getProgressSeconds() { return progressSeconds; }
    public void setProgressSeconds(Integer progressSeconds) { this.progressSeconds = progressSeconds; }
}
