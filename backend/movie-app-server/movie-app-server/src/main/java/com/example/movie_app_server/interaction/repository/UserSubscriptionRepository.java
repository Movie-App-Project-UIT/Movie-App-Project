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

    // Tìm các gói đăng ký đang ACTIVE và sắp hết hạn trong khoảng thời gian nhất định (phục vụ thông báo)
    List<UserSubscription> findByStatusAndEndDateBetween(
            com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus status,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end);

    List<UserSubscription> findByPlanAndStatus(
            com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan plan,
            com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus status);

    List<UserSubscription> findByPlanAndIsGift(
            com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan plan,
            Boolean isGift);
}
