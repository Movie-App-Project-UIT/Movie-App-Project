package com.example.movie_app_server.admin.repository;

import com.example.movie_app_server.admin.entity.AdminHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminHistoryRepository extends JpaRepository<AdminHistory, Long> {
    List<AdminHistory> findAllByOrderByCreatedAtDesc();
}
