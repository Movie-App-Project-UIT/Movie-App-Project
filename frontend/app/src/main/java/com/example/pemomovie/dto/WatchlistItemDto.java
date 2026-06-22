package com.example.pemomovie.dto;

import java.io.Serializable;

public class WatchlistItemDto implements Serializable {
    private Long id;
    private String addedAt;
    private MediaItemDto media;

    public WatchlistItemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public MediaItemDto getMedia() {
        return media;
    }

    public void setMedia(MediaItemDto media) {
        this.media = media;
    }
}
