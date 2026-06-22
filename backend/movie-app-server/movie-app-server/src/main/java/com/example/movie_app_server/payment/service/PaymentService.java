package com.example.movie_app_server.payment.service;

import com.example.movie_app_server.common.exception.AppException;
import com.example.movie_app_server.payment.config.VNPayConfig;
import com.example.movie_app_server.interaction.entity.subscription.SubscriptionPlan;
import com.example.movie_app_server.interaction.entity.subscription.UserSubscription;
import com.example.movie_app_server.interaction.entity.enums.SubscriptionStatus;
import com.example.movie_app_server.interaction.entity.enums.NotificationType;
import com.example.movie_app_server.payment.entity.Transaction;
import com.example.movie_app_server.interaction.repository.SubscriptionPlanRepository;
import com.example.movie_app_server.interaction.repository.UserSubscriptionRepository;
import com.example.movie_app_server.payment.repository.TransactionRepository;
import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.entity.enums.Tier;
import com.example.movie_app_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final VNPayConfig vnPayConfig;
    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final com.example.movie_app_server.interaction.service.NotificationService notificationService;

    public String createPaymentUrl(Long packageId, String paymentMethod, String firebaseUid, String ipAddress, Long customAmount) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        SubscriptionPlan subPackage = planRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Package not found", HttpStatus.NOT_FOUND));

        String txnRef = VNPayConfig.getRandomNumber(8);
        long amount = (customAmount != null && customAmount > 0) ? customAmount : subPackage.getPrice().longValue();

        return createVNPayPaymentUrl(txnRef, amount, user, subPackage, ipAddress);
    }

    private String createVNPayPaymentUrl(String txnRef, long amount, User user, SubscriptionPlan subPackage, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        long vnpAmount = amount * 100; // VNPay yêu cầu nhân 100

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        // vnp_Params.put("vnp_BankCode", "NCB"); // Bỏ dòng này đi để VNPay hiện màn hình chọn phương thức thanh toán (Có quét QR)
        vnp_Params.put("vnp_TxnRef", txnRef);
        vnp_Params.put("vnp_OrderInfo", "ThanhToanDonHang_" + txnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
                    
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        // Lưu transaction PENDING
        Transaction transaction = Transaction.builder()
                .user(user)
                .subscriptionPlan(subPackage)
                .amount(amount)
                .txnRef(txnRef)
                .paymentMethod("VNPAY")
                .status("PENDING")
                .build();
        transactionRepository.save(transaction);

        return paymentUrl;
    }

    private void upgradeUserVip(Transaction transaction) {
        User user = transaction.getUser();
        user.setTier(Tier.PREMIUM);
        userRepository.save(user);

        UserSubscription currentSub = userSubscriptionRepository
                .findFirstByUserAndStatusOrderByEndDateDesc(user, SubscriptionStatus.ACTIVE)
                .orElse(null);

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate;
        int durationDays = transaction.getSubscriptionPlan().getDurationDays();

        if (currentSub != null && currentSub.getEndDate().isAfter(LocalDateTime.now())) {
            startDate = currentSub.getEndDate();
            endDate = currentSub.getEndDate().plusDays(durationDays);
        } else {
            endDate = LocalDateTime.now().plusDays(durationDays);
        }

        UserSubscription newSub = UserSubscription.builder()
                .user(user)
                .plan(transaction.getSubscriptionPlan())
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        userSubscriptionRepository.save(newSub);

        // Lưu thông báo nâng cấp gói thành công vào Database
        notificationService.createNotification(
                user,
                "Nâng cấp thành công",
                "Chào mừng bạn đến với Premium! Bạn đã mở khóa tất cả đặc quyền xem phim.",
                NotificationType.SUBSCRIPTION_NEW_PLAN
        );

        // Xóa thông báo sắp hết hạn (nếu có) vì user đã gia hạn thành công
        notificationService.clearExpiringNotification(user);
    }

    public void simulateSuccess(Long packageId, String userUid) {
        User user = userRepository.findByFirebaseUid(userUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = planRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Transaction mockTxn = Transaction.builder()
                .user(user)
                .subscriptionPlan(plan)
                .amount(plan.getPrice().longValue())
                .paymentMethod("TEST")
                .status("SUCCESS")
                .txnRef("TEST-" + System.currentTimeMillis())
                .payDate(LocalDateTime.now())
                .build();
        
        transactionRepository.save(mockTxn);
        upgradeUserVip(mockTxn);
    }

    @Transactional
    public String processIpnWebhook(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (params.containsKey("vnp_SecureHashType")) {
            params.remove("vnp_SecureHashType");
        }
        if (params.containsKey("vnp_SecureHash")) {
            params.remove("vnp_SecureHash");
        }

        String signValue = VNPayConfig.hashAllFields(params, vnPayConfig.getSecretKey());
        if (!signValue.equals(secureHash)) {
            log.error("Invalid signature");
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid signature\"}";
        }

        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_ResponseCode = params.get("vnp_ResponseCode");

        Transaction transaction = transactionRepository.findByTxnRef(vnp_TxnRef).orElse(null);
        if (transaction == null) {
            return "{\"RspCode\":\"01\",\"Message\":\"Order not found\"}";
        }

        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
        if (transaction.getAmount() != vnpAmount) {
            return "{\"RspCode\":\"04\",\"Message\":\"Invalid amount\"}";
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            return "{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}";
        }

            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                transaction.setStatus("SUCCESS");
                transaction.setGatewayTransactionNo(params.get("vnp_TransactionNo"));
                transaction.setPayDate(LocalDateTime.now());
                upgradeUserVip(transaction);
            } else {
                transaction.setStatus("FAILED");
            }
            transactionRepository.save(transaction);

        return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
    }

    public String getVnPaySecretKey() {
        return vnPayConfig.getSecretKey();
    }
}
