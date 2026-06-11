package com.example.movie_app_server.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    // Cache to store OTPs: <Email, OTP>
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    public void generateAndSendOtp(String email) {
        // Generate 4 digit OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpCache.put(email, otp);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh khôi phục mật khẩu - PeMo Movie");
        message.setText("Mã xác minh (OTP) của bạn là: " + otp + "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        String savedOtp = otpCache.get(email);
        if (savedOtp != null && savedOtp.equals(otp)) {
            return true;
        }
        return false;
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}
