package com.example.movie_app_server.interaction.entity.subscription;

import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_usersub_user", columnList = "user_id"),
        @Index(name = "idx_usersub_plan", columnList = "plan_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private SubscriptionPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @CreationTimestamp(source = org.hibernate.annotations.SourceType.VM)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_gift", nullable = false)
    @Builder.Default
    private Boolean isGift = false;
}
