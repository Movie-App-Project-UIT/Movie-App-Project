package com.example.movie_app_server.media.repository;

import com.example.movie_app_server.media.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {

    // Tìm toàn bộ danh sách diễn viên/đạo diễn của 1 bộ phim
    List<Credit> findByMediaId(Long mediaId);

    // Tìm riêng đạo diễn (department = "Directing") của 1 bộ phim
    List<Credit> findByMediaIdAndDepartment(Long mediaId, String department);
}