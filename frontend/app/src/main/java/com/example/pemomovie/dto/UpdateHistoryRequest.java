package com.example.pemomovie.dto;

public class UpdateHistoryRequest {
    private Long mediaId;
    private Long episodeId;
    private Integer progressSeconds;
    private Integer totalDurationSeconds;

    public UpdateHistoryRequest(Long mediaId, Long episodeId, Integer progressSeconds, Integer totalDurationSeconds) {
        this.mediaId = mediaId;
        this.episodeId = episodeId;
        this.progressSeconds = progressSeconds;
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public Long getMediaId() { return mediaId; }
    public void setMediaId(Long mediaId) { this.mediaId = mediaId; }

    public Long getEpisodeId() { return episodeId; }
    public void setEpisodeId(Long episodeId) { this.episodeId = episodeId; }

    public Integer getProgressSeconds() { return progressSeconds; }
    public void setProgressSeconds(Integer progressSeconds) { this.progressSeconds = progressSeconds; }

    public Integer getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }
}
