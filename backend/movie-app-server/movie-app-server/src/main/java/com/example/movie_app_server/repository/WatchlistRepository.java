package com.example.movie_app_server.repository;

import com.example.movie_app_server.entity.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchList, Long> {
    // Tìm kiếm toàn bộ danh sách yêu thích của một User thông qua Firebase UID
    // JPA sẽ tự động thực hiện truy vấn JOIN qua bảng 'users' để kiểm tra điều kiện
    List<WatchList> findByUserFirebaseUidOrderByAddedAtDesc(String firebaseUid);

    // Kiểm tra xem bộ phim này đã nằm trong danh sách yêu thích của User chưa
    boolean existsByUserFirebaseUidAndMediaId(String firebaseUid, Long mediaId);

    // Tìm bản ghi cụ thể để phục vụ chức năng xóa phim khỏi danh sách yêu thích
    Optional<WatchList> findByUserFirebaseUidAndMediaId(String firebaseUid, Long mediaId);
}
