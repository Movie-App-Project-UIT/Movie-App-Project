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

    public void sendForgotPasswordOtp(String email) {
        String otp = generateOtp(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh khôi phục mật khẩu - PeMo Movie");
        message.setText("Mã xác minh (OTP) của bạn là: " + otp + "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }

    public void sendRegisterOtp(String email) {
        String otp = generateOtp(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh Đăng ký tài khoản - PeMo Movie");
        message.setText("Chào mừng bạn đến với PeMo Movie!\n\nMã xác minh (OTP) đăng ký tài khoản của bạn là: " + otp + "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }

    private String generateOtp(String email) {
        String otp = String.format("%04d", new Random().nextInt(10000));
        otpCache.put(email, otp);
        return otp;
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
