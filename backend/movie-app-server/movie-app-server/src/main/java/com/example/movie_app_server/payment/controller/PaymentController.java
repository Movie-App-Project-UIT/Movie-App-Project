package com.example.movie_app_server.payment.controller;

import com.example.movie_app_server.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private String getUid() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/create-url")
    public ResponseEntity<String> createPaymentUrl(@RequestParam Long packageId, 
                                                   @RequestParam(defaultValue = "VNPAY") String paymentMethod,
                                                   @RequestParam(required = false) Long amount,
                                                   HttpServletRequest request) {
        // Lấy IP của user
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        String paymentUrl = paymentService.createPaymentUrl(packageId, paymentMethod, getUid(), ipAddress, amount);
        return ResponseEntity.ok(paymentUrl);
    }



    // IPN Webhook: VNPay Server sẽ gọi trực tiếp vào đây để báo kết quả
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<String> vnpayIpn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            fields.put(entry.getKey(), entry.getValue()[0]);
        }

        String result = paymentService.processIpnWebhook(fields);
        return ResponseEntity.ok(result);
    }

    // Return URL: Trình duyệt của user chuyển hướng về đây sau khi thanh toán xong
    @GetMapping("/vnpay-return")
    public org.springframework.http.ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        String signValue = com.example.movie_app_server.payment.config.VNPayConfig.hashAllFields(verifyParams, paymentService.getVnPaySecretKey());

        String redirectUrl;
        if (!signValue.equals(secureHash)) {
            // Chữ ký không hợp lệ → thất bại
            redirectUrl = "pemomovie://payment?status=failed&reason=invalid_signature";
        } else {
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            if ("00".equals(responseCode)) {
                // Thanh toán thành công → nâng cấp user
                boolean upgraded = paymentService.processReturnUrl(txnRef);
                redirectUrl = upgraded
                        ? "pemomovie://payment?status=success"
                        : "pemomovie://payment?status=success"; // Kể cả đã SUCCESS rồi (IPN xử lý trước) vẫn redirect success
            } else {
                redirectUrl = "pemomovie://payment?status=failed&reason=payment_failed";
            }
        }

        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }
}
