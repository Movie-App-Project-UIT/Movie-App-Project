package com.example.movie_app_server.media.controller;

import com.example.movie_app_server.media.dto.SubtitleDto;
import com.example.movie_app_server.media.dto.SubtitleRequest;
import com.example.movie_app_server.media.service.SubtitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/subtitles")
@RequiredArgsConstructor
public class AdminSubtitleController {

    private final SubtitleService subtitleService;

    @PostMapping
    public ResponseEntity<SubtitleDto> addSubtitle(@Valid @RequestBody SubtitleRequest request) {
        return ResponseEntity.ok(subtitleService.addSubtitle(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtitle(@PathVariable Long id) {
        subtitleService.deleteSubtitle(id);
        return ResponseEntity.ok().build();
    }
}
