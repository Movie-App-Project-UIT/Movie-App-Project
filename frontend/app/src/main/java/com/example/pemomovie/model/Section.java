package com.example.pemomovie.model;

import com.example.pemomovie.dto.MediaItemDto;
import java.util.List;

public class Section {
    private String title;
    private List<MediaItemDto> movies;

    public Section(String title, List<MediaItemDto> movies) {
        this.title = title;
        this.movies = movies;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MediaItemDto> getMovies() {
        return movies;
    }

    public void setMovies(List<MediaItemDto> movies) {
        this.movies = movies;
    }
}
