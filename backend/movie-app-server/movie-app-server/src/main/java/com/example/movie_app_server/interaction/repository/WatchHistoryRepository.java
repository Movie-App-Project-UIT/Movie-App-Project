package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    // Lấy lịch sử xem của user (Phim vừa xem xếp lên đầu)
    List<WatchHistory> findByUserIdOrderByLastWatchedAtDesc(Long userId);

    // Tìm lịch sử xem của một phim lẻ cụ thể
    Optional<WatchHistory> findByUserIdAndMediaId(Long userId, Long mediaId);

    // Tìm lịch sử xem của một tập phim bộ cụ thể
    Optional<WatchHistory> findByUserIdAndEpisodeId(Long userId, Long episodeId);
}
