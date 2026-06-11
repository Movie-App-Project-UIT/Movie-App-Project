package com.example.movie_app_server.media.repository;

import com.example.movie_app_server.media.entity.Subtitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtitleRepository extends JpaRepository<Subtitle, Long> {
    List<Subtitle> findByMediaId(Long mediaId);
    List<Subtitle> findByEpisodeId(Long episodeId);

    boolean existsByMediaIdAndLanguage(Long mediaId, String language);
    boolean existsByEpisodeIdAndLanguage(Long episodeId, String language);
}
