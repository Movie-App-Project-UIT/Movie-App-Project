package com.example.movie_app_server.interaction.repository;

import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUser(User user);
    
    // Tìm gói đăng ký đang ACTIVE của user
    Optional<UserSubscription> findFirstByUserAndStatusOrderByEndDateDesc(User user, com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus status);
}
