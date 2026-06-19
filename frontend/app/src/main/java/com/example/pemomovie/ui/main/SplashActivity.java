package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;
import com.example.pemomovie.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {
    // Hàm onCreate là điểm bắt đầu. Bất cứ khi nào màn hình này được mở lên,
    // Android sẽ chạy các lệnh bên trong hàm này đầu tiên.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Nối file giao diện XML (activity_splash.xml) vào file Java này
        // Lệnh này giúp hiển thị logo, nền màu gradient và 3 dấu chấm lên màn hình
        setContentView(R.layout.activity_splash);

        View dot1 = findViewById(R.id.dot1);
        View dot2 = findViewById(R.id.dot2);
        View dot3 = findViewById(R.id.dot3);
        // 2. Nạp hiệu ứng mờ ảo từ file blink.xml cho cả 3 dấu chấm
        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.blink);
        Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.blink);
        Animation anim3 = AnimationUtils.loadAnimation(this, R.anim.blink);
        // 3. Đặt độ trễ thời gian (Quan trọng nhất)
        // Nếu không có độ trễ, 3 dấu chấm sẽ nhấp nháy cùng 1 lúc nhìn rất chán.
        // Ta bắt dấu chấm 2 trễ 0.2s, dấu chấm 3 trễ 0.4s để tạo hiệu ứng "sóng lượn"
        anim2.setStartOffset(200);
        anim3.setStartOffset(400);
        // 4. Kích hoạt chạy hiệu ứng
        dot1.startAnimation(anim1);
        dot2.startAnimation(anim2);
        dot3.startAnimation(anim3);
        // 2. Tạo một bộ đếm thời gian (Handler)
        // Looper.getMainLooper() đảm bảo bộ đếm này chạy trên luồng giao diện chính (Main Thread)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Toàn bộ code trong khối run() này sẽ được thực thi
                // SAU KHI đếm ngược đủ thời gian quy định ở dưới cùng.
                com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                Intent intent;
                if (currentUser != null) {
                    // Nếu đã đăng nhập, đọc role từ SharedPreferences
                    android.content.SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    String role = prefs.getString("user_role", "USER");
                    
                    if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) {
                        intent = new Intent(SplashActivity.this, com.example.pemomovie.ui.main.AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, HomeActivity.class);
                    }
                } else {
                    // Nếu chưa đăng nhập, chuyển vào LoginActivity
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                // 4. Bắt đầu thực hiện lệnh chuyển trang
                startActivity(intent);
                // Hàm finish() giúp huỷ luôn màn hình Splash khỏi bộ nhớ.
                finish();
            }
        }, 2500); // 2500 là thời gian đếm ngược tính bằng mili-giây (Tương đương 2.5 giây)
        // Bạn có thể tăng giảm số này tuỳ ý để màn hình Splash hiện lâu hay mau.
    }
}
