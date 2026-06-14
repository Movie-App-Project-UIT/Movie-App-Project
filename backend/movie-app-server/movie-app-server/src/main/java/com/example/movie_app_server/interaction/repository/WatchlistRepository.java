package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    // Lấy danh sách phim yêu thích của user (Mới lưu xếp lên đầu)
    List<Watchlist> findByUserIdOrderByAddedAtDesc(Long userId);

    // Kiểm tra xem user đã lưu phim này vào danh sách chưa
    Optional<Watchlist> findByUserIdAndMediaId(Long userId, Long mediaId);

    // Tìm kiếm toàn bộ danh sách yêu thích của một User thông qua Firebase UID
    // JPA sẽ tự động thực hiện truy vấn JOIN qua bảng 'users' để kiểm tra điều kiện
    List<Watchlist> findByUserFirebaseUidOrderByAddedAtDesc(String firebaseUid);

    // Kiểm tra xem bộ phim này đã nằm trong danh sách yêu thích của User chưa
    boolean existsByUserFirebaseUidAndMediaId(String firebaseUid, Long mediaId);

    // Tìm bản ghi cụ thể để phục vụ chức năng xóa phim khỏi danh sách yêu thích
    Optional<Watchlist> findByUserFirebaseUidAndMediaId(String firebaseUid, Long mediaId);

    // Tìm tất cả người dùng đang theo dõi một bộ phim (phục vụ chức năng Thông báo)
    List<Watchlist> findByMediaId(Long mediaId);
}
