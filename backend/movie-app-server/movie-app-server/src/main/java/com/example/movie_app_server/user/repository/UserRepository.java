package com.example.movie_app_server.user.repository;

import com.example.movie_app_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user dựa trên mã định danh Firebase UID nhận từ Client Token
    Optional<User> findByFirebaseUid(String firebaseUid);

    // Kiểm tra hoặc tìm kiếm nhanh bằng Email
    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
            "(:isPremium IS NULL OR (:isPremium = true AND u.tier = 'PREMIUM') OR (:isPremium = false AND u.tier = 'FREE')) AND " +
            "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<User> searchAndFilterUsers(@org.springframework.data.repository.query.Param("isPremium") Boolean isPremium,
                                              @org.springframework.data.repository.query.Param("search") String search, org.springframework.data.domain.Pageable pageable);
}
