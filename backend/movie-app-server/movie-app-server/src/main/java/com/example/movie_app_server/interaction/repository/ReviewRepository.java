package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 1. Lấy danh sách bình luận gốc của một bộ phim (Các bình luận có parent_id IS NULL)
    // Sắp xếp theo thời gian mới nhất lên đầu để người dùng dễ theo dõi
    List<Review> findByMediaIdAndParentIsNullOrderByCreatedAtDesc(Long mediaId);

    // 2. Lấy danh sách bình luận gốc cụ thể cho một TẬP PHIM (Phim bộ)
    List<Review> findByMediaIdAndEpisodeIdAndParentIsNullOrderByCreatedAtDesc(Long mediaId, Long episodeId);

    // 3. Nếu sau này bạn làm giao diện "Xem thêm câu trả lời", hàm này dùng để load
    // riêng các reply của một bình luận cha cụ thể
    List<Review> findByParentIdOrderByCreatedAtAsc(Long parentId); // Xếp từ cũ đến mới để đọc theo luồng hội thoại

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.media.id = :mediaId AND r.parent IS NULL AND r.isHidden = false ORDER BY r.createdAt DESC")
    List<Review> findRootReviewsByMediaIdWithUser(@Param("mediaId") Long mediaId);

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.media.id = :mediaId")
    List<Review> findAllByMediaIdWithUser(@Param("mediaId") Long mediaId);

    // Lấy danh sách bình luận của một user cụ thể
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
}
