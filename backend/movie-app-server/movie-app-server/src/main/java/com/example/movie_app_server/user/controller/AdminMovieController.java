package com.example.movie_app_server.user.controller;

import com.example.movie_app_server.media.dto.MediaItemDto;
import com.example.movie_app_server.media.dto.MediaDetailResponse;
import com.example.movie_app_server.admin.dto.AdminMovieSaveRequest;
import com.example.movie_app_server.admin.dto.AdminEpisodeSaveRequest;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.Season;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.media.service.MediaService;
import com.example.movie_app_server.media.service.TmdbSyncService;
import com.example.movie_app_server.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final TmdbSyncService tmdbSyncService;
    private final AdminHistoryService adminHistoryService;

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<MediaItemDto>> getAllMovies() {
        List<MediaItemDto> movies = mediaRepository.findAll().stream()
                .map(mediaService::convertToItemDto)
                .toList();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<MediaDetailResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaDetailAdmin(id));
    }

    @PutMapping("/{id}/soft-delete")
    @CacheEvict(value = "homepageData", allEntries = true)
    public ResponseEntity<Void> softDeleteMovie(@PathVariable Long id) {
        return mediaRepository.findById(id).map(media -> {
            boolean isNowDeleted = !media.isDeleted();
            media.setDeleted(isNowDeleted); // Toggle
            mediaRepository.save(media);
            adminHistoryService.logAction(
                isNowDeleted ? "DELETE" : "RESTORE",
                "MOVIE",
                media.getId().toString(),
                (isNowDeleted ? "Xóa phim: " : "Khôi phục phim: ") + media.getTitle()
            );
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/preview-tmdb")
    public ResponseEntity<?> previewFromTmdb(@RequestParam Integer tmdbId, @RequestParam(required = false) String type) {
        try {
            Media media;
            if ("TV_SERIES".equalsIgnoreCase(type)) {
                media = tmdbSyncService.previewTvSeriesFromTmdb(tmdbId);
            } else if ("MOVIE".equalsIgnoreCase(type)) {
                media = tmdbSyncService.previewMovieFromTmdb(tmdbId);
            } else {
                try {
                    media = tmdbSyncService.previewMovieFromTmdb(tmdbId);
                } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                    media = tmdbSyncService.previewTvSeriesFromTmdb(tmdbId);
                }
            }
            return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.internalServerError().body(sw.toString());
        }
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    @CacheEvict(value = "homepageData", allEntries = true)
    public ResponseEntity<MediaDetailResponse> createMovie(@RequestBody AdminMovieSaveRequest request) {
        Media media;
        if ("TV_SERIES".equalsIgnoreCase(request.getMediaType())) {
            media = tmdbSyncService.previewTvSeriesFromTmdb(request.getTmdbId());
        } else if ("MOVIE".equalsIgnoreCase(request.getMediaType())) {
            media = tmdbSyncService.previewMovieFromTmdb(request.getTmdbId());
        } else {
            try {
                media = tmdbSyncService.previewMovieFromTmdb(request.getTmdbId());
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                media = tmdbSyncService.previewTvSeriesFromTmdb(request.getTmdbId());
            }
        }
        
        if (request.getMediaType() != null) {
            media.setMediaType(com.example.movie_app_server.media.entity.enums.MediaType.valueOf(request.getMediaType()));
        }
        
        if (media.getMediaType() == com.example.movie_app_server.media.entity.enums.MediaType.MOVIE) {
            media.setExpectedEpisodes(1);
        } else {
            media.setExpectedEpisodes(request.getExpectedEpisodes() != null ? request.getExpectedEpisodes() : 1);
        }
        
        if (request.getTmdbId() != null && mediaRepository.findByTmdbId(request.getTmdbId()).isPresent()) {
            throw new com.example.movie_app_server.common.exception.AppException("Phim này đã tồn tại trong hệ thống!", org.springframework.http.HttpStatus.CONFLICT);
        }
        media.setVideoUrl(request.getVideoUrl());
        media.setPremium(request.isPremium());
        media.setDeleted(request.isDeleted());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            media.setTitle(request.getTitle());
        }
        if (request.getOverview() != null && !request.getOverview().isEmpty()) {
            media.setOverview(request.getOverview());
        }
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            media.setLanguage(request.getLanguage());
        }

        if (request.getSubtitles() != null && !request.getSubtitles().isEmpty()) {
            for (AdminMovieSaveRequest.AdminSubtitleRequest subReq : request.getSubtitles()) {
                media.getSubtitles().add(com.example.movie_app_server.media.entity.Subtitle.builder()
                        .language(subReq.getLanguage())
                        .fileUrl(subReq.getFileUrl())
                        .media(media)
                        .build());
            }
        }
        media = mediaRepository.save(media);
        adminHistoryService.logAction("CREATE", "MOVIE", media.getId().toString(), "Thêm phim mới: " + media.getTitle());
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    @CacheEvict(value = "homepageData", allEntries = true)
    public ResponseEntity<MediaDetailResponse> updateMovie(@PathVariable Long id, @RequestBody AdminMovieSaveRequest request) {
        Media media = mediaRepository.findById(id).orElse(null);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (request.getTmdbId() != null && request.getTmdbId() > 0) {
            try {
                Media freshTmdb;
                if (media.getMediaType() == com.example.movie_app_server.media.entity.enums.MediaType.TV_SERIES) {
                    freshTmdb = tmdbSyncService.previewTvSeriesFromTmdb(request.getTmdbId());
                } else {
                    freshTmdb = tmdbSyncService.previewMovieFromTmdb(request.getTmdbId());
                }
                
                media.setTmdbId(freshTmdb.getTmdbId());
                media.setPosterPath(freshTmdb.getPosterPath());
                media.setBackdropPath(freshTmdb.getBackdropPath());
                media.setVoteAverage(freshTmdb.getVoteAverage());
                media.setReleaseDate(freshTmdb.getReleaseDate());
                media.setCountry(freshTmdb.getCountry());
                media.setDuration(freshTmdb.getDuration());
                media.setTrailerUrl(freshTmdb.getTrailerUrl());
                
                if (request.getMediaType() != null) {
                    media.setMediaType(com.example.movie_app_server.media.entity.enums.MediaType.valueOf(request.getMediaType()));
                } else {
                    media.setMediaType(freshTmdb.getMediaType());
                }
                
                media.getGenres().clear();
                if (freshTmdb.getGenres() != null) {
                    media.getGenres().addAll(freshTmdb.getGenres());
                }

                media.getCredits().clear();
                if (freshTmdb.getCredits() != null) {
                    for (com.example.movie_app_server.media.entity.Credit c : freshTmdb.getCredits()) {
                        c.setMedia(media);
                        media.getCredits().add(c);
                    }
                }
            } catch (Exception e) {
                // Ignore TMDB fetch error during update if it fails, but ideally it works
            }
        }
        
        if (request.getVideoUrl() != null) {
            media.setVideoUrl(request.getVideoUrl());
        }
        media.setPremium(request.isPremium());
        media.setDeleted(request.isDeleted());
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            media.setTitle(request.getTitle());
        }
        if (request.getOverview() != null && !request.getOverview().isEmpty()) {
            media.setOverview(request.getOverview());
        }
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            media.setLanguage(request.getLanguage());
        }
        
        if (request.getMediaType() != null) {
            media.setMediaType(com.example.movie_app_server.media.entity.enums.MediaType.valueOf(request.getMediaType()));
        }

        if (media.getMediaType() == com.example.movie_app_server.media.entity.enums.MediaType.MOVIE) {
            media.setExpectedEpisodes(1);
            if (media.getSeasons() != null && !media.getSeasons().isEmpty()) {
                for (Season s : media.getSeasons()) {
                    if (s.getEpisodes() != null) {
                        for (int i = s.getEpisodes().size() - 1; i >= 0; i--) {
                            if (i > 0 || s.getSeasonNumber() > 1) {
                                s.getEpisodes().remove(i);
                            }
                        }
                    }
                }
            }
        } else if (request.getExpectedEpisodes() != null) {
            media.setExpectedEpisodes(request.getExpectedEpisodes());
            if (media.getSeasons() != null && !media.getSeasons().isEmpty()) {
                for (Season s : media.getSeasons()) {
                    if (s.getEpisodes() != null) {
                        for (int i = s.getEpisodes().size() - 1; i >= request.getExpectedEpisodes(); i--) {
                            s.getEpisodes().remove(i);
                        }
                    }
                }
            }
        }

        media.getSubtitles().clear();
        if (request.getSubtitles() != null && !request.getSubtitles().isEmpty()) {
            for (AdminMovieSaveRequest.AdminSubtitleRequest subReq : request.getSubtitles()) {
                media.getSubtitles().add(com.example.movie_app_server.media.entity.Subtitle.builder()
                        .language(subReq.getLanguage())
                        .fileUrl(subReq.getFileUrl())
                        .media(media)
                        .build());
            }
        }
        
        if (media.getMediaType() == com.example.movie_app_server.media.entity.enums.MediaType.TV_SERIES) {
            updateMediaPremiumStatus(media);
            updateMediaDeletedStatus(media);
        }
        
        media = mediaRepository.save(media);
        adminHistoryService.logAction("UPDATE", "MOVIE", media.getId().toString(), "Cập nhật thông tin phim: " + media.getTitle());
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    @PostMapping("/{id}/episodes")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<MediaDetailResponse> createEpisode(@PathVariable Long id, @RequestBody AdminEpisodeSaveRequest request) {
        Media media = mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        
        Season season = null;
        if (media.getSeasons().isEmpty()) {
            season = new Season();
            season.setSeasonNumber(1);
            season.setMedia(media);
            season.setEpisodes(new java.util.ArrayList<>());
            media.getSeasons().add(season);
        } else {
            season = media.getSeasons().get(0);
        }

        Episode episode = new Episode();
        episode.setSeason(season);
        episode.setEpisodeNumber(season.getEpisodes().size() + 1);
        episode.setTitle(request.getTitle());
        episode.setOverview(request.getOverview());
        episode.setVideoUrl(request.getVideoUrl());
        episode.setPremium(request.isPremium());
        episode.setDeleted(request.isDeleted());

        if (request.getSubtitles() != null && !request.getSubtitles().isEmpty()) {
            for (AdminMovieSaveRequest.AdminSubtitleRequest subReq : request.getSubtitles()) {
                episode.getSubtitles().add(com.example.movie_app_server.media.entity.Subtitle.builder()
                        .language(subReq.getLanguage())
                        .fileUrl(subReq.getFileUrl())
                        .episode(episode)
                        .build());
            }
        }

        season.getEpisodes().add(episode);
        updateMediaPremiumStatus(media);
        mediaRepository.save(media);
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    @PutMapping("/{id}/episodes/{episodeId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<MediaDetailResponse> updateEpisode(@PathVariable Long id, @PathVariable Long episodeId, @RequestBody AdminEpisodeSaveRequest request) {
        Media media = mediaRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        Episode episode = null;
        for (Season s : media.getSeasons()) {
            for (Episode e : s.getEpisodes()) {
                if (e.getId().equals(episodeId)) {
                    episode = e;
                    break;
                }
            }
        }
        if (episode == null) return ResponseEntity.notFound().build();

        episode.setTitle(request.getTitle());
        episode.setOverview(request.getOverview());
        episode.setVideoUrl(request.getVideoUrl());
        episode.setPremium(request.isPremium());
        episode.setDeleted(request.isDeleted());

        episode.getSubtitles().clear();
        if (request.getSubtitles() != null && !request.getSubtitles().isEmpty()) {
            for (AdminMovieSaveRequest.AdminSubtitleRequest subReq : request.getSubtitles()) {
                episode.getSubtitles().add(com.example.movie_app_server.media.entity.Subtitle.builder()
                        .language(subReq.getLanguage())
                        .fileUrl(subReq.getFileUrl())
                        .episode(episode)
                        .build());
            }
        }

        updateMediaDeletedStatus(media);

        updateMediaPremiumStatus(media);
        mediaRepository.save(media);
        return ResponseEntity.ok(mediaService.convertToDetailResponse(media));
    }

    private void updateMediaPremiumStatus(Media media) {
        if (media.getMediaType() == com.example.movie_app_server.media.entity.enums.MediaType.TV_SERIES) {
            int totalEpisodes = 0;
            int premiumEpisodes = 0;
            if (media.getSeasons() != null) {
                for (Season s : media.getSeasons()) {
                    if (s.getEpisodes() != null) {
                        for (Episode e : s.getEpisodes()) {
                            if (!e.isDeleted()) {
                                totalEpisodes++;
                                if (e.isPremium()) {
                                    premiumEpisodes++;
                                }
                            }
                        }
                    }
                }
            }
            if (totalEpisodes > 0 && premiumEpisodes > (totalEpisodes / 2.0)) {
                media.setPremium(true);
            } else {
                media.setPremium(false);
            }
        }
    }

    private void updateMediaDeletedStatus(Media media) {
        boolean allDeleted = true;
        for (Season s : media.getSeasons()) {
            for (Episode e : s.getEpisodes()) {
                if (!e.isDeleted()) {
                    allDeleted = false;
                    break;
                }
            }
        }
        if (allDeleted && !media.getSeasons().isEmpty() && !media.getSeasons().get(0).getEpisodes().isEmpty()) {
            media.setDeleted(true);
        } else if (!allDeleted && media.isDeleted()) {
            media.setDeleted(false);
        }
    }
}
