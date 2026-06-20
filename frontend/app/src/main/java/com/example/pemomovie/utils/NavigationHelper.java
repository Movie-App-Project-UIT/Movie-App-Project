package com.example.pemomovie.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.example.pemomovie.R;
import com.example.pemomovie.ui.main.FavoriteActivity;
import com.example.pemomovie.ui.main.HomeActivity;
import com.example.pemomovie.ui.main.MovieActivity;
import com.example.pemomovie.ui.main.ProfileActivity;
import com.example.pemomovie.ui.main.WatchingActivity;

public class NavigationHelper {
    public static void setupBottomNavigation(Activity activity) {
        ImageButton btnHome = activity.findViewById(R.id.btnHome);
        ImageButton btnGenres = activity.findViewById(R.id.btnGenres);
        ImageButton btnFavorites = activity.findViewById(R.id.btnFavorites);
        ImageButton btnWatching = activity.findViewById(R.id.btnWatching);

        // Tự động làm sáng (Selected) icon của màn hình hiện tại
        if (activity instanceof HomeActivity) {
            if (btnHome != null) btnHome.setSelected(true);
        } else if (activity instanceof MovieActivity) {
            if (btnGenres != null) btnGenres.setSelected(true);
        } else if (activity instanceof FavoriteActivity) {
            if (btnFavorites != null) btnFavorites.setSelected(true);
        } else if (activity instanceof ProfileActivity) {
            if (btnWatching != null) btnWatching.setSelected(true);
        }

        // Bắt sự kiện click nút Home
        if (btnHome != null) {
            addBounceEffect(btnHome);
            btnHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    startActivityWithAnim(activity, HomeActivity.class);
                }
            });
        }

        // Nút Movies (Thay cho Genres cũ)
        if (btnGenres != null) {
            addBounceEffect(btnGenres);
            btnGenres.setOnClickListener(v -> {
                if (!(activity instanceof MovieActivity)) {
                    startActivityWithAnim(activity, MovieActivity.class);
                }
            });
        }

        // Nút Favorites (Yêu thích)
        if (btnFavorites != null) {
            addBounceEffect(btnFavorites);
            btnFavorites.setOnClickListener(v -> {
                if (!(activity instanceof FavoriteActivity)) {
                    startActivityWithAnim(activity, FavoriteActivity.class);
                }
            });
        }

        // Nút Profile (Thay cho Watching cũ)
        if (btnWatching != null) {
            addBounceEffect(btnWatching);
            btnWatching.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    startActivityWithAnim(activity, WatchingActivity.class);
                }
            });
        }
    }

    private static void startActivityWithAnim(Activity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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