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

    // API để App gọi lấy URL thanh toán VNPay
    @PostMapping("/create-url")
    public ResponseEntity<String> createPaymentUrl(@RequestParam Long packageId, 
                                                   @RequestParam(defaultValue = "VNPAY") String paymentMethod,
                                                   HttpServletRequest request) {
        // Lấy IP của user
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        String paymentUrl = paymentService.createPaymentUrl(packageId, paymentMethod, getUid(), ipAddress);
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

    // IPN Webhook: MoMo Server sẽ gọi POST JSON vào đây
    @PostMapping("/momo-ipn")
    public ResponseEntity<String> momoIpn(@RequestBody Map<String, Object> payload) {
        paymentService.processMoMoIpnWebhook(payload);
        // Trả về HTTP 204 No Content theo chuẩn MoMo (hoặc 200 OK rỗng)
        return ResponseEntity.noContent().build();
    }

    // Return URL: Trình duyệt của user chuyển hướng về đây sau khi thanh toán xong
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            // Hiển thị giao diện hoặc redirect về App (DeepLink)
            return ResponseEntity.ok("Thanh toán thành công! Bạn có thể quay lại Ứng dụng.");
        } else {
            return ResponseEntity.ok("Thanh toán thất bại hoặc đã bị hủy.");
        }
    }
}
