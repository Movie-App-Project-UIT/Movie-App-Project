package com.example.movie_app_server.media.repository;

import com.example.movie_app_server.media.entity.Media;
import com.example.movie_app_server.media.entity.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    // Phân loại phim: lấy danh sách toàn bộ phim lẻ hoặc toàn bộ phim bộ
    List<Media> findByMediaType(MediaType mediaType);

    // Tìm kiếm phim theo tiêu đề (Không phân biệt chữ hoa / chữ thường)
    List<Media> findByTitleContainingIgnoreCase(String title);

    // Tìm kiếm phim bằng ID lấy từ hệ thống TMDB để tránh trùng lặp khi đồng bộ
    Optional<Media> findByTmdbId(Integer tmdbId);

    @Query("SELECT DISTINCT m FROM Media m " +
            "LEFT JOIN m.genres g " +
            "WHERE (:genreId IS NULL OR g.id = :genreId) " +
            "AND (:countryId IS NULL OR m.country.id = :countryId) " +
            "AND (:ageRatingId IS NULL OR m.ageRating.id = :ageRatingId) " +
            "AND (:releaseYear IS NULL OR YEAR(m.releaseDate) = :releaseYear) " +
            "AND (:mediaType IS NULL OR CAST(m.mediaType AS string) = :mediaType) " + // ĐÃ THÊM DÒNG NÀY
            "AND (:isPlayable IS NULL OR " +
            "  (:isPlayable = true AND (" +
            "    (m.mediaType = 'MOVIE' AND m.videoUrl IS NOT NULL AND TRIM(m.videoUrl) <> '') OR " +
            "    (m.mediaType = 'TV_SERIES' AND EXISTS (SELECT e FROM Episode e WHERE e.season.media = m AND e.videoUrl IS NOT NULL AND TRIM(e.videoUrl) <> ''))" +
            "  )) OR " +
            "  (:isPlayable = false AND (" +
            "    (m.mediaType = 'MOVIE' AND (m.videoUrl IS NULL OR TRIM(m.videoUrl) = '')) OR " +
            "    (m.mediaType = 'TV_SERIES' AND NOT EXISTS (SELECT e FROM Episode e WHERE e.season.media = m AND e.videoUrl IS NOT NULL AND TRIM(e.videoUrl) <> ''))" +
            "  ))" +
            ")")
    Page<Media> filterMediaDynamically(
            @Param("genreId") Long genreId,
            @Param("countryId") Long countryId,
            @Param("ageRatingId") Long ageRatingId,
            @Param("releaseYear") Integer releaseYear,
            @Param("isPlayable") Boolean isPlayable,
            @Param("mediaType") String mediaType, // ĐÃ THÊM THAM SỐ NÀY
            Pageable pageable
    );
}
