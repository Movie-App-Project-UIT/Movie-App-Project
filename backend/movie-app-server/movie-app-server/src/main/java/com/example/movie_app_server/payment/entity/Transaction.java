package com.example.movie_app_server.payment.entity;

import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(nullable = false)
    private Long amount; // Số tiền giao dịch

    @Column(name = "txn_ref", length = 50, unique = true)
    private String txnRef; // Mã đơn hàng tự sinh gửi đi cổng thanh toán

    @Column(name = "gateway_transaction_no", length = 50)
    private String gatewayTransactionNo; // Mã giao dịch của Cổng thanh toán (VNPay/MoMo) trả về

    @Column(name = "payment_method", length = 20, nullable = false)
    @Builder.Default
    private String paymentMethod = "VNPAY"; // VNPAY, MOMO

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED

    @CreationTimestamp(source = org.hibernate.annotations.SourceType.VM)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "pay_date")
    private LocalDateTime payDate;
}
