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

    @Transactional
    public WatchHistory updateHistory(Long userId, Long mediaId, Long episodeId, Integer progressSeconds) {
        User user = userRepository.findById(userId)
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
            existingHistory = watchHistoryRepository.findByUserIdAndEpisodeId(userId, episodeId);
        } else {
            existingHistory = watchHistoryRepository.findByUserIdAndMediaId(userId, mediaId);
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

        return watchHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<WatchHistory> getUserHistory(Long userId) {
        return watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(userId);
    }
}
