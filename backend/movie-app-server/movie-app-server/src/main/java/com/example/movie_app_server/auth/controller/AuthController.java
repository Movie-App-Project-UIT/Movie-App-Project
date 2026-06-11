package com.example.movie_app_server.auth.controller;

import com.example.movie_app_server.auth.service.OtpService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    private Map<String, String> msg(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            // Check if user exists in Firebase
            FirebaseAuth.getInstance().getUserByEmail(email);
            // Send OTP
            otpService.generateAndSendOtp(email);
            return ResponseEntity.ok(msg("Đã gửi mã xác nhận đến email!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(msg("Email không tồn tại trong hệ thống!"));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        if (otpService.verifyOtp(email, code)) {
            return ResponseEntity.ok(msg("Mã xác nhận chính xác!"));
        } else {
            return ResponseEntity.badRequest().body(msg("Mã xác nhận không đúng!"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        String newPassword = payload.get("newPassword");

        if (otpService.verifyOtp(email, code)) {
            try {
                UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(user.getUid())
                        .setPassword(newPassword);
                FirebaseAuth.getInstance().updateUser(request);
                otpService.clearOtp(email);
                return ResponseEntity.ok(msg("Đổi mật khẩu thành công!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(msg("Lỗi khi cập nhật mật khẩu!"));
            }
        } else {
            return ResponseEntity.badRequest().body(msg("Mã xác nhận không đúng hoặc đã hết hạn!"));
        }
    }
}
