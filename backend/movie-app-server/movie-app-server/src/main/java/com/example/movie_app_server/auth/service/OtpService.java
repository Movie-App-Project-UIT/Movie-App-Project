package com.example.movie_app_server.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    // Thời hạn OTP: 5 phút
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Lớp nội bộ lưu trữ OTP kèm thời điểm hết hạn.
     */
    private record OtpEntry(String code, LocalDateTime expiryTime) {}

    // Cache lưu OTP: <Email, OtpEntry(mã OTP + thời gian hết hạn)>
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();

    public void sendForgotPasswordOtp(String email) {
        String otp = generateOtp(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh khôi phục mật khẩu - PeMo Movie");
        message.setText("Mã xác minh (OTP) của bạn là: " + otp
                + "\n\nMã này có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút."
                + "\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }

    public void sendRegisterOtp(String email) {
        String otp = generateOtp(email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Mã xác minh Đăng ký tài khoản - PeMo Movie");
        message.setText("Chào mừng bạn đến với PeMo Movie!"
                + "\n\nMã xác minh (OTP) đăng ký tài khoản của bạn là: " + otp
                + "\n\nMã này có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút."
                + "\nVui lòng không chia sẻ mã này cho bất kỳ ai.");
        mailSender.send(message);
    }

    private String generateOtp(String email) {
        String otp = String.format("%04d", new Random().nextInt(10000));
        // Lưu OTP kèm thời điểm hết hạn (hiện tại + 5 phút)
        otpCache.put(email, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpCache.get(email);
        if (entry == null) {
            return false;
        }
        // Kiểm tra OTP đã hết hạn chưa
        if (LocalDateTime.now().isAfter(entry.expiryTime())) {
            otpCache.remove(email); // Tự động xóa OTP hết hạn
            return false;
        }
        return entry.code().equals(otp);
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}
