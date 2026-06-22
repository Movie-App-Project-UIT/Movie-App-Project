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
    List<Media> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title, Pageable pageable);
    List<Media> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);
    List<Media> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Tìm kiếm phim bằng ID lấy từ hệ thống TMDB để tránh trùng lặp khi đồng bộ
    Optional<Media> findByTmdbId(Integer tmdbId);

    // Tìm các phim thuộc một thể loại
    List<Media> findByGenres_Id(Long genreId);

    // Tìm các phim ĐANG HOẠT ĐỘNG và thuộc một thể loại
    List<Media> findByIsDeletedFalseAndGenres_Id(Long genreId);

    // Tìm các phim KHÔNG thuộc một thể loại
    @Query("SELECT m FROM Media m WHERE m.isDeleted = false AND :genre NOT MEMBER OF m.genres")
    List<Media> findMediaNotContainingGenre(@Param("genre") com.example.movie_app_server.media.entity.Genre genre);

    // --- Homepage API Queries ---
    // Phim được đánh giá cao nhất (Top Rated) > 8.0
    List<Media> findTop10ByIsDeletedFalseAndVoteAverageGreaterThanEqualOrderByVoteAverageDesc(Float voteAverage);
    
    // Lấy Top 10 phim đánh giá cao (không bắt buộc >= 8.0)
    List<Media> findTop10ByIsDeletedFalseOrderByVoteAverageDesc();

    // Phim mới cập nhật (Recently Added) - Dùng createdAt thay vì id vì TiDB AUTO_INCREMENT không liên tục
    List<Media> findTop10ByIsDeletedFalseOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT m FROM Media m " +
            "LEFT JOIN m.genres g " +
            "WHERE m.isDeleted = false " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:#{#genreIds == null || #genreIds.isEmpty()} = true OR g.id IN :genreIds) " +
            "AND (:#{#countryIds == null || #countryIds.isEmpty()} = true OR m.country.id IN :countryIds) " +
            "AND (:#{#languages == null || #languages.isEmpty()} = true OR m.language IN :languages) " +
            "AND (:ageRatingId IS NULL OR m.ageRating.id = :ageRatingId) " +
            "AND (CAST(:startDate AS date) IS NULL OR m.releaseDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR m.releaseDate <= :endDate) " +
            "AND (:mediaType IS NULL OR CAST(m.mediaType AS string) = :mediaType) " +
            "AND (:isPremium IS NULL OR m.isPremium = :isPremium) " +
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
            @Param("keyword") String keyword,
            @Param("genreIds") java.util.List<Long> genreIds,
            @Param("countryIds") java.util.List<Long> countryIds,
            @Param("languages") java.util.List<String> languages,
            @Param("ageRatingId") Long ageRatingId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("isPlayable") Boolean isPlayable,
            @Param("mediaType") String mediaType,
            @Param("isPremium") Boolean isPremium,
            Pageable pageable
    );

    // Lấy danh sách phim Trending (được xem nhiều nhất trong khoảng thời gian gần đây)
    @Query("SELECT wh.media FROM WatchHistory wh " +
            "WHERE wh.lastWatchedAt >= :since " +
            "GROUP BY wh.media " +
            "ORDER BY COUNT(wh) DESC")
    List<Media> findTrendingMedia(@Param("since") java.time.LocalDateTime since, Pageable pageable);
}
