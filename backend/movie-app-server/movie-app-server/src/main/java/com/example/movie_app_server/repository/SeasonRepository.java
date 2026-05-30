package com.example.movie_app_server.repository;

import com.example.movie_app_server.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    // Lấy tất cả các phần của một bộ phim bộ, sắp xếp từ Phần 1, Phần 2 trở lên
    List<Season> findByMediaIdOrderBySeasonNumberAsc(Long mediaId);

    // Tìm một phần phim cụ thể dựa theo mã phim và số thứ tự phần
    Optional<Season> findByMediaIdAndSeasonNumber(Long mediaId, Integer seasonNumber);
}
