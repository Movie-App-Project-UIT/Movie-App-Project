package com.example.movie_app_server.interaction.service;

import com.example.movie_app_server.interaction.entity.WatchHistory;
import com.example.movie_app_server.interaction.repository.WatchHistoryRepository;
import com.example.movie_app_server.media.entity.Episode;
import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.repository.EpisodeRepository;
import com.example.movie_app_server.media.repository.MediaRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final EpisodeRepository episodeRepository;
    private final com.example.movie_app_server.media.service.MediaService mediaService;

    @Transactional
    public com.example.movie_app_server.interaction.dto.WatchHistoryItemDto updateHistory(String firebaseUid, Long mediaId, Long episodeId, Integer progressSeconds) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        Episode episode = null;
        if (episodeId != null) {
            episode = episodeRepository.findById(episodeId)
                    .orElseThrow(() -> new RuntimeException("Episode not found"));
        }

        Optional<WatchHistory> existingHistory;
        if (episode != null) {
            existingHistory = watchHistoryRepository.findByUserIdAndEpisodeId(user.getId(), episodeId);
        } else {
            existingHistory = watchHistoryRepository.findByUserIdAndMediaId(user.getId(), mediaId);
        }

        WatchHistory history;
        if (existingHistory.isPresent()) {
            history = existingHistory.get();
            history.setProgressSeconds(progressSeconds);
        } else {
            history = WatchHistory.builder()
                    .user(user)
                    .media(media)
                    .episode(episode)
                    .progressSeconds(progressSeconds)
                    .build();
        }

        WatchHistory savedHistory = watchHistoryRepository.save(history);
        return convertToDto(savedHistory);
    }

    @Transactional(readOnly = true)
    public List<com.example.movie_app_server.interaction.dto.WatchHistoryItemDto> getUserHistory(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(user.getId())
                .stream().map(this::convertToDto).toList();
    }

    private com.example.movie_app_server.interaction.dto.WatchHistoryItemDto convertToDto(WatchHistory h) {
        return com.example.movie_app_server.interaction.dto.WatchHistoryItemDto.builder()
                .id(h.getId())
                .progressSeconds(h.getProgressSeconds())
                .lastWatchedAt(h.getLastWatchedAt())
                .media(mediaService.convertToItemDto(h.getMedia()))
                .episode(h.getEpisode() != null ? mediaService.convertToEpisodeDto(h.getEpisode()) : null)
                .build();
    }
}
