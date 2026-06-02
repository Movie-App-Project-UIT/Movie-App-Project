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

}
