package com.example.movie_app_server.repository;

import com.example.movie_app_server.entity.Media;
import com.example.movie_app_server.entity.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    // Phân loại phim: lấy danh sách toàn bộ phim lẻ hoặc toàn bộ phim bộ
    List<Media> findByMediaType(MediaType mediaType);

    // Tìm kiếm phim theo tiêu đề (Không phân biệt chữ hoa / chữ thường)
    List<Media> findByTitleContainingIgnoreCase(String title);

    // Tìm kiếm phim bằng ID lấy từ hệ thống TMDB để tránh trùng lặp khi đồng bộ
    Optional<Media> findByTmdbId(Integer tmdbId);
}
