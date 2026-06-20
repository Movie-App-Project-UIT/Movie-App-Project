package com.example.pemomovie.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.example.pemomovie.R;
import com.example.pemomovie.ui.admin.AdminMovieActivity;
import com.example.pemomovie.ui.admin.AdminCategoryActivity;
import com.example.pemomovie.ui.admin.AdminSubscriptionActivity;
import com.example.pemomovie.ui.admin.AdminUserActivity;
import com.example.pemomovie.ui.admin.AdminProfileActivity;

public class AdminNavigationHelper {
    public static void setupBottomNavigation(Activity activity) {
        View btnAdminDashboard = activity.findViewById(R.id.btnAdminDashboard);
        View btnAdminSubscriptions = activity.findViewById(R.id.btnAdminSubscriptions);
        View btnAdminUsers = activity.findViewById(R.id.btnAdminUsers);
        View btnAdminProfile = activity.findViewById(R.id.btnAdminProfile);

        // Tự động làm sáng (Selected) icon của màn hình hiện tại
        if (activity instanceof com.example.pemomovie.ui.main.AdminDashboardActivity) {
            if (btnAdminDashboard != null) btnAdminDashboard.setSelected(true);
        } else if (activity instanceof AdminSubscriptionActivity) {
            if (btnAdminSubscriptions != null) btnAdminSubscriptions.setSelected(true);
        } else if (activity instanceof AdminUserActivity) {
            if (btnAdminUsers != null) btnAdminUsers.setSelected(true);
        } else if (activity instanceof AdminProfileActivity) {
            if (btnAdminProfile != null) btnAdminProfile.setSelected(true);
        }

        // Click listeners
        if (btnAdminDashboard != null) {
            addBounceEffect(btnAdminDashboard);
            btnAdminDashboard.setOnClickListener(v -> {
                if (!(activity instanceof com.example.pemomovie.ui.main.AdminDashboardActivity)) {
                    startActivityWithAnim(activity, com.example.pemomovie.ui.main.AdminDashboardActivity.class);
                }
            });
        }

        if (btnAdminSubscriptions != null) {
            addBounceEffect(btnAdminSubscriptions);
            btnAdminSubscriptions.setOnClickListener(v -> {
                if (!(activity instanceof AdminSubscriptionActivity)) {
                    startActivityWithAnim(activity, AdminSubscriptionActivity.class);
                }
            });
        }

        if (btnAdminUsers != null) {
            addBounceEffect(btnAdminUsers);
            btnAdminUsers.setOnClickListener(v -> {
                if (!(activity instanceof AdminUserActivity)) {
                    startActivityWithAnim(activity, AdminUserActivity.class);
                }
            });
        }

        if (btnAdminProfile != null) {
            addBounceEffect(btnAdminProfile);
            btnAdminProfile.setOnClickListener(v -> {
                if (!(activity instanceof AdminProfileActivity)) {
                    startActivityWithAnim(activity, AdminProfileActivity.class);
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
