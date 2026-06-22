package com.example.pemomovie.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.GlobalSearchAdapter;
import com.example.pemomovie.adapter.NotificationAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.NotificationDto;
import com.example.pemomovie.dto.PageResponseDto;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.example.pemomovie.ui.main.NotificationActivity;
import com.example.pemomovie.ui.main.ProfileActivity;
import com.example.pemomovie.ui.main.UpgradePremiumActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GlobalHeaderHelper {

    private Activity activity;
    
    // Search components
    private EditText edtSearch;
    private PopupWindow searchPopup;
    private RecyclerView rvSearchResults;
    private GlobalSearchAdapter searchAdapter;
    private ProgressBar pbLoading;
    private TextView tvEmptySearch;
    private View btnCloseSearch;
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Notification components
    private List<NotificationDto> dbNotifications = new ArrayList<>();
    private boolean hasAdminGift = false;
    private boolean hasPremiumNotif = false;
    private boolean hasExpiringNotif = false;
    private TextView tvNotificationBadge;

    public GlobalHeaderHelper(Activity activity) {
        this.activity = activity;
    }

    public void setupGlobalHeader(View headerView) {
        if (headerView == null) return;

        // 1. Setup Search
        edtSearch = headerView.findViewById(R.id.edtGlobalSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) {
                        debounceHandler.removeCallbacks(searchRunnable);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        if (searchPopup != null && searchPopup.isShowing()) {
                            searchPopup.dismiss();
                        }
                        return;
                    }
                    searchRunnable = () -> performSearch(query);
                    debounceHandler.postDelayed(searchRunnable, 500); // 0.5s debounce
                }
            });
        }

        // 2. Setup Notification Button
        ImageButton btnNotification = headerView.findViewById(R.id.btnGlobalNotification);
        tvNotificationBadge = headerView.findViewById(R.id.tvGlobalNotificationBadge);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(this::showNotificationDropdown);
        }

        // 3. Setup Logo Click to Home
        ImageView ivHeaderLogo = headerView.findViewById(R.id.ivHeaderLogo);
        if (ivHeaderLogo != null) {
            ivHeaderLogo.setOnClickListener(v -> {
                Intent intent = new Intent(activity, com.example.pemomovie.ui.main.HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            });
        }

        // 3. Setup Profile Button
        ImageView btnProfile = headerView.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(this::showProfileDropdown);
            loadProfileAvatar(btnProfile);
        }

        // Fetch notifications when setting up the header
        fetchNotifications();
    }

    // ================== PROFILE LOGIC ==================

    private void loadProfileAvatar(ImageView btnProfile) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
            String photoUrl = currentUser.getPhotoUrl().toString().trim();
            if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
            }
            int paddingPx = (int) (2 * activity.getResources().getDisplayMetrics().density);
            btnProfile.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            btnProfile.setBackgroundResource(R.drawable.bg_circle_avatar_border);
            Glide.with(activity)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_avatar)
                    .circleCrop()
                    .into(btnProfile);
        } else {
            btnProfile.setBackground(null);
            btnProfile.setPadding(0, 0, 0, 0);
            btnProfile.setImageResource(R.drawable.ic_avatar);
        }
    }

    private void showProfileDropdown(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_profile_dropdown, null);

        int width = (int) (240 * activity.getResources().getDisplayMetrics().density);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(10);
        }

        ImageView ivDropdownAvatar = popupView.findViewById(R.id.ivDropdownAvatar);
        TextView tvDropdownName = popupView.findViewById(R.id.tvDropdownName);
        TextView tvDropdownEmail = popupView.findViewById(R.id.tvDropdownEmail);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (name != null && !name.isEmpty()) {
                tvDropdownName.setText(name);
            } else {
                tvDropdownName.setText("User");
            }

            if (email != null && !email.isEmpty()) {
                tvDropdownEmail.setText(email);
            } else {
                tvDropdownEmail.setText("No Email");
            }

            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                int paddingPx = (int) (2 * activity.getResources().getDisplayMetrics().density);
                ivDropdownAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                ivDropdownAvatar.setBackgroundResource(R.drawable.bg_circle_avatar_border);
                Glide.with(activity)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(ivDropdownAvatar);
            } else {
                ivDropdownAvatar.setBackground(null);
                ivDropdownAvatar.setPadding(0, 0, 0, 0);
                ivDropdownAvatar.setImageResource(R.drawable.ic_avatar);
            }
        }

        LinearLayout layoutBtnProfile = popupView.findViewById(R.id.layoutBtnProfile);
        LinearLayout layoutBtnLogout = popupView.findViewById(R.id.layoutBtnLogout);

        layoutBtnProfile.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(activity, ProfileActivity.class);
            activity.startActivity(intent);
        });

        layoutBtnLogout.setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        int yoff = (int) (8 * activity.getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchorView, 0, yoff);
    }

    // ================== NOTIFICATION LOGIC ==================

    public void fetchNotifications() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Long userId = Long.parseLong(response.body().getId());
                        apiService.getUserNotifications(userId).enqueue(new Callback<List<NotificationDto>>() {
                            @Override
                            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> response2) {
                                if (response2.isSuccessful() && response2.body() != null) {
                                    dbNotifications.clear();
                                    dbNotifications.addAll(response2.body());
                                    
                                    hasExpiringNotif = false;
                                    hasAdminGift = false;
                                    hasPremiumNotif = false;
                                    int unreadCount = 0;
                                    
                                    for (NotificationDto notif : dbNotifications) {
                                        if (notif.getRead() == null || !notif.getRead()) {
                                            unreadCount++;
                                            if ("GIFT_RECEIVED".equals(notif.getType())) {
                                                hasAdminGift = true;
                                            } else if ("SUBSCRIPTION_NEW_PLAN".equals(notif.getType())) {
                                                hasPremiumNotif = true;
                                            } else if ("SUBSCRIPTION_EXPIRING".equals(notif.getType())) {
                                                hasExpiringNotif = true;
                                            }
                                        }
                                    }
                                    updateNotificationBadge(unreadCount);
                                }
                            }
                            @Override
                            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {}
                        });
                    } catch (NumberFormatException e) {
                        Log.e("GlobalHeaderHelper", "Lỗi định dạng User ID", e);
                    }
                }
            }
            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {}
        });
    }

    private void updateNotificationBadge(int unreadCount) {
        if (tvNotificationBadge != null) {
            if (unreadCount > 0) {
                tvNotificationBadge.setText(String.valueOf(unreadCount));
                tvNotificationBadge.setVisibility(View.VISIBLE);
            } else {
                tvNotificationBadge.setVisibility(View.GONE);
            }
        }
    }

    private void showNotificationDropdown(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_notification_dropdown, null);

        int width = (int) (320 * activity.getResources().getDisplayMetrics().density);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(10);
        }

        View layoutEmptyNotification = popupView.findViewById(R.id.layoutEmptyNotification);
        RecyclerView rvDropdownNotifications = popupView.findViewById(R.id.rvDropdownNotifications);

        List<NotificationDto> unreadNotifications = new ArrayList<>();
        if (dbNotifications != null) {
            for (NotificationDto notif : dbNotifications) {
                if (notif.getRead() == null || !notif.getRead()) {
                    unreadNotifications.add(notif);
                }
            }
        }

        if (!unreadNotifications.isEmpty()) {
            if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.GONE);
            if (rvDropdownNotifications != null) {
                rvDropdownNotifications.setVisibility(View.VISIBLE);
                rvDropdownNotifications.setLayoutManager(new LinearLayoutManager(activity));
                
                // Show up to 5 latest unread notifications
                List<NotificationDto> displayList = unreadNotifications.size() > 5 
                        ? unreadNotifications.subList(0, 5) 
                        : unreadNotifications;
                        
                NotificationAdapter adapter = new NotificationAdapter(
                        activity,
                        displayList,
                        new NotificationAdapter.OnNotificationClickListener() {
                            @Override
                            public void onNotificationClick(NotificationDto notification) {
                                popupWindow.dismiss();
                                Intent intent = new Intent(activity, NotificationActivity.class);
                                activity.startActivity(intent);
                            }

                            @Override
                            public void onActionClick(NotificationDto notification) {
                                popupWindow.dismiss();
                                if ("SUBSCRIPTION_EXPIRING".equals(notification.getType())) {
                                    showExpiringPremiumBottomSheet(notification.getMessage() != null ? notification.getMessage() : "Gói Premium của bạn sắp hết hạn.");
                                } else if ("SUBSCRIPTION_NEW_PLAN".equals(notification.getType())) {
                                    Intent intent = new Intent(activity, ProfileActivity.class);
                                    activity.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(activity, NotificationActivity.class);
                                    activity.startActivity(intent);
                                }
                            }
                        }
                );
                rvDropdownNotifications.setAdapter(adapter);
            }
        } else {
            if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.VISIBLE);
            if (rvDropdownNotifications != null) rvDropdownNotifications.setVisibility(View.GONE);
        }

        View tvMarkAsRead = popupView.findViewById(R.id.tvMarkAsRead);
        if (tvMarkAsRead != null) {
            tvMarkAsRead.setOnClickListener(v -> {
                for (NotificationDto notif : dbNotifications) {
                    if (notif.getRead() == null || !notif.getRead()) {
                        notif.setRead(true);
                        markNotificationAsReadOnBackend(notif.getId());
                    }
                }
                hasExpiringNotif = false;
                hasAdminGift = false;
                hasPremiumNotif = false;
                updateNotificationBadge(0);
                
                // Cập nhật giao diện trực tiếp thành dạng trống (Không tự động đóng popup)
                if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.VISIBLE);
                if (rvDropdownNotifications != null) rvDropdownNotifications.setVisibility(View.GONE);
                
                Toast.makeText(activity, "Đã đánh dấu đọc tất cả", Toast.LENGTH_SHORT).show();
            });
        }

        View btnViewAllNotifications = popupView.findViewById(R.id.btnViewAllNotifications);
        if (btnViewAllNotifications != null) {
            btnViewAllNotifications.setOnClickListener(v -> {
                popupWindow.dismiss();
                Intent intent = new Intent(activity, NotificationActivity.class);
                activity.startActivity(intent);
            });
        }

        int xoff = anchorView.getWidth() - width;
        int yoff = (int) (8 * activity.getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchorView, xoff, yoff);
    }

    private void markNotificationAsReadOnBackend(Long id) {
        if (id == null) return;
        ApiClient.getApiService().markNotificationAsRead(id).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }

    private void showExpiringPremiumBottomSheet(String message) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        View bottomSheetView = activity.getLayoutInflater().inflate(R.layout.layout_bottom_sheet_expiring_premium, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnShowListener(dialog -> {
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        TextView tvExpiringSubtitle = bottomSheetView.findViewById(R.id.tvExpiringSubtitle);
        if (tvExpiringSubtitle != null && message != null) {
            tvExpiringSubtitle.setText(message);
        }

        View btnCloseBottomSheet = bottomSheetView.findViewById(R.id.btnCloseBottomSheet);
        if (btnCloseBottomSheet != null) {
            btnCloseBottomSheet.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        View btnLater = bottomSheetView.findViewById(R.id.btnLater);
        if (btnLater != null) {
            btnLater.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        View btnRenewPremium = bottomSheetView.findViewById(R.id.btnRenewPremium);
        if (btnRenewPremium != null) {
            btnRenewPremium.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(activity, UpgradePremiumActivity.class);
                activity.startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }

    // ================== SEARCH LOGIC ==================

    private void performSearch(String query) {
        showSearchPopup();
        pbLoading.setVisibility(View.VISIBLE);
        tvEmptySearch.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        ApiClient.getApiService().filterMedia(query, null, null, null, null, null, null, 0, 10)
            .enqueue(new Callback<PageResponseDto<MediaItemDto>>() {
                @Override
                public void onResponse(Call<PageResponseDto<MediaItemDto>> call, Response<PageResponseDto<MediaItemDto>> response) {
                    pbLoading.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<MediaItemDto> results = response.body().getContent();
                        if (results == null || results.isEmpty()) {
                            tvEmptySearch.setVisibility(View.VISIBLE);
                        } else {
                            rvSearchResults.setVisibility(View.VISIBLE);
                            searchAdapter.updateData(results);
                        }
                    } else {
                        tvEmptySearch.setVisibility(View.VISIBLE);
                        tvEmptySearch.setText("Có lỗi xảy ra khi tìm kiếm");
                    }
                }

                @Override
                public void onFailure(Call<PageResponseDto<MediaItemDto>> call, Throwable t) {
                    pbLoading.setVisibility(View.GONE);
                    tvEmptySearch.setVisibility(View.VISIBLE);
                    tvEmptySearch.setText("Lỗi kết nối");
                }
            });
    }

    private void showSearchPopup() {
        if (searchPopup == null) {
            View popupView = LayoutInflater.from(activity).inflate(R.layout.layout_search_dropdown, null);
            rvSearchResults = popupView.findViewById(R.id.rvSearchResults);
            pbLoading = popupView.findViewById(R.id.pbSearchLoading);
            tvEmptySearch = popupView.findViewById(R.id.tvEmptySearch);
            btnCloseSearch = popupView.findViewById(R.id.btnCloseSearch);

            searchAdapter = new GlobalSearchAdapter(activity, new ArrayList<>());
            rvSearchResults.setLayoutManager(new LinearLayoutManager(activity));
            rvSearchResults.setAdapter(searchAdapter);

            btnCloseSearch.setOnClickListener(v -> {
                searchPopup.dismiss();
                if (edtSearch != null) {
                    edtSearch.setText("");
                    edtSearch.clearFocus();
                }
            });

            searchPopup = new PopupWindow(popupView, 
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            searchPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            searchPopup.setOutsideTouchable(true);
            searchPopup.setElevation(20f);
        }

        if (!searchPopup.isShowing() && edtSearch != null) {
            searchPopup.showAsDropDown(edtSearch, 0, 8);
        }
    }
}
