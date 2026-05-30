package com.example.movie_app_server.repository;

import com.example.movie_app_server.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    // Lấy danh sách lịch sử xem phim của một User, sắp xếp theo mốc thời gian mới xem nhất lộn lên đầu
    List<WatchHistory> findByUserFirebaseUidOrderByLastWatchedAtDesc(String firebaseUid);

    // Kiểm tra xem User đã từng xem bộ phim này chưa để cập nhật tiếp tiến trình (hoặc tạo mới nếu xem lần đầu)
    Optional<WatchHistory> findByUserFirebaseUidAndMediaId(String firebaseUid, Long mediaId);
}
