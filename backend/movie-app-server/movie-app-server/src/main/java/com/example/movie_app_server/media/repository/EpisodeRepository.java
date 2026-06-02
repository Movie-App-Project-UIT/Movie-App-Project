package com.example.movie_app_server.media.repository;

import com.example.movie_app_server.media.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    // Lấy toàn bộ danh sách tập của một phần phim, sắp xếp theo thứ tự tập tăng dần
    List<Episode> findBySeasonIdOrderByEpisodeNumberAsc(Long seasonId);

    // Tìm một tập phim cụ thể để phát video
    Optional<Episode> findBySeasonIdAndEpisodeNumber(Long seasonId, Integer episodeNumber);
}