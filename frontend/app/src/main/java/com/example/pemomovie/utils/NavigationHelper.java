package com.example.pemomovie.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.example.pemomovie.R;
import com.example.pemomovie.ui.main.FavoriteActivity;
import com.example.pemomovie.ui.main.HomeActivity;

public class NavigationHelper {
    public static void setupBottomNavigation(Activity activity) {
        ImageButton btnHome = activity.findViewById(R.id.btnHome);
        ImageButton btnGenres = activity.findViewById(R.id.btnGenres);
        ImageButton btnFavorites = activity.findViewById(R.id.btnFavorites);
        ImageButton btnWatching = activity.findViewById(R.id.btnWatching);
        // Tự động làm sáng (Selected) icon của màn hình hiện tại
        if (activity instanceof HomeActivity) {
            if (btnHome != null) btnHome.setSelected(true);
        } else if (activity instanceof FavoriteActivity) {
            if (btnFavorites != null) btnFavorites.setSelected(true);
        }
        // Bắt sự kiện click nút Home
        if (btnHome != null) {
            addBounceEffect(btnHome);
            btnHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    // Dùng cờ này để tái sử dụng Activity cũ nếu có, tránh tạo mới tốn RAM
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });
        }
        // Nút Genres (Thể loại)
        if (btnGenres != null) {
            addBounceEffect(btnGenres);
            btnGenres.setOnClickListener(v -> {
                // TODO: Chuyển hướng tới GenresActivity khi bạn tạo file
            });
        }
        // Nút Favorites (Yêu thích)
        if (btnFavorites != null) {
            addBounceEffect(btnFavorites);
            btnFavorites.setOnClickListener(v -> {
                if (!(activity instanceof FavoriteActivity)) {
                    Intent intent = new Intent(activity, FavoriteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });
        }
        // Nút Watching (Xem phim)
        if (btnWatching != null) {
            addBounceEffect(btnWatching);
            btnWatching.setOnClickListener(v -> {
                // TODO: Chuyển hướng tới WatchingActivity khi bạn tạo file
            });
        }
    }
    // Hàm tạo hiệu ứng co giãn (Bounce) khi chạm vào icon
    private static void addBounceEffect(View button) {
        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }
            return false;
        });
    }
}