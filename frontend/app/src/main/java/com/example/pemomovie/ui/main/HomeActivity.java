package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.SectionAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.model.Section;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.example.pemomovie.utils.NavigationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private androidx.viewpager2.widget.ViewPager2 bannerViewPager;
    private RecyclerView sectionListHome;
    private Handler handler;
    private Runnable sliderRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bannerViewPager = findViewById(R.id.bannerViewPager);
        sectionListHome = findViewById(R.id.sectionListHome);
        sectionListHome.setLayoutManager(new LinearLayoutManager(this));

        handler = new Handler(Looper.getMainLooper());

        NavigationHelper.setupBottomNavigation(this);
        ImageView btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> showProfileDropdown(v));

        sliderRunnable = () -> {
            if (bannerViewPager != null && bannerViewPager.getAdapter() != null) {
                int currentItem = bannerViewPager.getCurrentItem();
                int totalItems = bannerViewPager.getAdapter().getItemCount();
                if (totalItems > 0) {
                    int nextItem = (currentItem + 1) % totalItems;
                    bannerViewPager.setCurrentItem(nextItem, true);
                }
            }
        };

        ImageButton btnNotification = findViewById(R.id.btnNotification);
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> showNotificationDropdown(v));
        }

        // Đồng bộ danh sách phim yêu thích với Backend khi vừa mở App
        com.example.pemomovie.utils.FavoriteManager.syncFavoritesWithBackend(this, null);

        fetchHomepageData();
    }

    private void fetchHomepageData() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getHomepageData().enqueue(new Callback<Map<String, List<MediaItemDto>>>() {
            @Override
            public void onResponse(Call<Map<String, List<MediaItemDto>>> call, Response<Map<String, List<MediaItemDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, List<MediaItemDto>> data = response.body();
                    List<Section> sections = new ArrayList<>();

                    List<MediaItemDto> trending = data.get("trending");
                    if (trending != null && !trending.isEmpty()) {
                        sections.add(new Section("Đang thịnh hành", trending));

                        // Set banner ViewPager
                        List<MediaItemDto> bannerMovies = trending.size() > 5 ? trending.subList(0, 5) : trending;
                        com.example.pemomovie.adapter.BannerAdapter bannerAdapter = new com.example.pemomovie.adapter.BannerAdapter(HomeActivity.this, bannerMovies);
                        bannerViewPager.setAdapter(bannerAdapter);
                        
                        int startPosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % bannerMovies.size());
                        bannerViewPager.setCurrentItem(startPosition, false);
                        
                        Button btnPlay = findViewById(R.id.btnPlay);
                        Button btnDetail = findViewById(R.id.btnDetail);

                        bannerViewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);
                                handler.removeCallbacks(sliderRunnable);
                                handler.postDelayed(sliderRunnable, 5000);
                                
                                int realPosition = position % bannerMovies.size();
                                MediaItemDto currentMovie = bannerMovies.get(realPosition);
                                
                                if (btnDetail != null) {
                                    btnDetail.setOnClickListener(v -> {
                                        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
                                        intent.putExtra("MOVIE_ID", currentMovie.getId());
                                        startActivity(intent);
                                    });
                                }

                                if (btnPlay != null) {
                                    btnPlay.setOnClickListener(v -> {
                                        Intent intent = new Intent(HomeActivity.this, PlayActivity.class);
                                        intent.putExtra("MOVIE_ID", currentMovie.getId());
                                        startActivity(intent);
                                    });
                                }
                            }
                        });
                    }


                    List<MediaItemDto> topRated = data.get("topRated");
                    if (topRated != null && !topRated.isEmpty()) {
                        sections.add(new Section("Đánh giá cao", topRated));
                    }

                    SectionAdapter adapter = new SectionAdapter(HomeActivity.this, sections);
                    sectionListHome.setAdapter(adapter);
                } else {
                    Log.e("HomeActivity", "Failed to fetch homepage data");
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<MediaItemDto>>> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching homepage data", t);
            }
        });
    }

    private void showProfileDropdown(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_profile_dropdown, null);

        int width = (int) (240 * getResources().getDisplayMetrics().density);
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
                int paddingPx = (int) (2 * getResources().getDisplayMetrics().density);
                ivDropdownAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                ivDropdownAvatar.setBackgroundResource(R.drawable.bg_circle_avatar_border);
                Glide.with(HomeActivity.this)
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
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        layoutBtnLogout.setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        int yoff = (int) (8 * getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchorView, 0, yoff);
    }

    private List<com.example.pemomovie.dto.NotificationDto> dbNotifications = new java.util.ArrayList<>();
    private boolean hasAdminGift = false;
    private boolean hasPremiumNotif = false;
    private boolean hasExpiringNotif = false;

    private void showNotificationDropdown(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.layout_notification_dropdown, null);

        int width = (int) (320 * getResources().getDisplayMetrics().density);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(10);
        }

        View layoutEmptyNotification = popupView.findViewById(R.id.layoutEmptyNotification);
        RecyclerView rvDropdownNotifications = popupView.findViewById(R.id.rvDropdownNotifications);

        List<com.example.pemomovie.dto.NotificationDto> unreadList = new java.util.ArrayList<>();
        if (dbNotifications != null) {
            for (com.example.pemomovie.dto.NotificationDto notif : dbNotifications) {
                if (notif.getRead() == null || !notif.getRead()) {
                    unreadList.add(notif);
                }
            }
        }

        if (!unreadList.isEmpty()) {
            if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.GONE);
            if (rvDropdownNotifications != null) {
                rvDropdownNotifications.setVisibility(View.VISIBLE);
                rvDropdownNotifications.setLayoutManager(new LinearLayoutManager(this));
                
                // Show up to 5 latest notifications in dropdown
                List<com.example.pemomovie.dto.NotificationDto> displayList = unreadList.size() > 5 
                        ? unreadList.subList(0, 5) 
                        : unreadList;
                        
                com.example.pemomovie.adapter.NotificationAdapter adapter = new com.example.pemomovie.adapter.NotificationAdapter(
                        this,
                        displayList,
                        new com.example.pemomovie.adapter.NotificationAdapter.OnNotificationClickListener() {
                            @Override
                            public void onNotificationClick(com.example.pemomovie.dto.NotificationDto notification) {
                                popupWindow.dismiss();
                                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onActionClick(com.example.pemomovie.dto.NotificationDto notification) {
                                popupWindow.dismiss();
                                if ("SUBSCRIPTION_EXPIRING".equals(notification.getType())) {
                                    showExpiringPremiumBottomSheet(notification.getMessage() != null ? notification.getMessage() : "Gói Premium của bạn sắp hết hạn.");
                                } else if ("SUBSCRIPTION_NEW_PLAN".equals(notification.getType())) {
                                    Intent intent = new Intent(HomeActivity.this, PaymentSuccessActivity.class);
                                    intent.putExtra("IS_VIEW_PRIVILEGE", true);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                                    startActivity(intent);
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
                for (com.example.pemomovie.dto.NotificationDto notif : dbNotifications) {
                    if (notif.getRead() == null || !notif.getRead()) {
                        markNotificationAsReadOnBackend(notif.getId());
                        notif.setRead(true);
                    }
                }
                hasExpiringNotif = false;
                hasAdminGift = false;
                hasPremiumNotif = false;
                updateNotificationBadge(0);
                
                dbNotifications.clear();
                if (rvDropdownNotifications != null && rvDropdownNotifications.getAdapter() != null) {
                    rvDropdownNotifications.getAdapter().notifyDataSetChanged();
                    rvDropdownNotifications.setVisibility(View.GONE);
                }
                if (layoutEmptyNotification != null) {
                    layoutEmptyNotification.setVisibility(View.VISIBLE);
                }
                
                Toast.makeText(HomeActivity.this, "Đã đánh dấu đọc tất cả", Toast.LENGTH_SHORT).show();
            });
        }

        View btnViewAllNotifications = popupView.findViewById(R.id.btnViewAllNotifications);
        if (btnViewAllNotifications != null) {
            btnViewAllNotifications.setOnClickListener(v -> {
                popupWindow.dismiss();
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        int xoff = anchorView.getWidth() - width;
        int yoff = (int) (8 * getResources().getDisplayMetrics().density);
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

    private void fetchNotifications() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Long userId = Long.parseLong(response.body().getId());
                        apiService.getUserNotifications(userId).enqueue(new Callback<List<com.example.pemomovie.dto.NotificationDto>>() {
                            @Override
                            public void onResponse(Call<List<com.example.pemomovie.dto.NotificationDto>> call, Response<List<com.example.pemomovie.dto.NotificationDto>> response2) {
                                if (response2.isSuccessful() && response2.body() != null) {
                                    dbNotifications.clear();
                                    dbNotifications.addAll(response2.body());
                                    
                                    hasExpiringNotif = false;
                                    hasAdminGift = false;
                                    hasPremiumNotif = false;
                                    int unreadCount = 0;
                                    
                                    for (com.example.pemomovie.dto.NotificationDto notif : dbNotifications) {
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
                            public void onFailure(Call<List<com.example.pemomovie.dto.NotificationDto>> call, Throwable t) {}
                        });
                    } catch (NumberFormatException e) {
                        Log.e("HomeActivity", "Lỗi định dạng User ID", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {}
        });
    }

    private void updateNotificationBadge(int unreadCount) {
        TextView tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        
        if (tvNotificationBadge != null) {
            if (unreadCount > 0) {
                tvNotificationBadge.setText(String.valueOf(unreadCount));
                tvNotificationBadge.setVisibility(View.VISIBLE);
            } else {
                tvNotificationBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && sliderRunnable != null) {
            handler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeProfileAvatar();
        fetchNotifications();
        if (handler != null && sliderRunnable != null) {
            handler.postDelayed(sliderRunnable, 5000);
        }
    }

    private void loadHomeProfileAvatar() {
        ImageView btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                int paddingPx = (int) (2 * getResources().getDisplayMetrics().density);
                btnProfile.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                btnProfile.setBackgroundResource(R.drawable.bg_circle_avatar_border);
                Glide.with(HomeActivity.this)
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
    }

    private String getTimeAgo(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) return "Vừa xong";
        try {
            // Remove fractional seconds if present (e.g. .123456)
            if (createdAt.contains(".")) {
                createdAt = createdAt.substring(0, createdAt.indexOf("."));
            }
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            // sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); // Remove this to use local time
            java.util.Date date = sdf.parse(createdAt);
            if (date == null) return "Vừa xong";

            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            if (diff < 0) diff = 0; // Prevent negative time difference

            if (diff < 60 * 1000) {
                return "Vừa xong";
            } else if (diff < 60 * 60 * 1000) {
                return (diff / (60 * 1000)) + " phút trước";
            } else if (diff < 24 * 60 * 60 * 1000) {
                return (diff / (60 * 60 * 1000)) + " giờ trước";
            } else {
                return (diff / (24 * 60 * 60 * 1000)) + " ngày trước";
            }
        } catch (Exception e) {
            return "Vừa xong";
        }
    }

    private void showExpiringPremiumBottomSheet(String message) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_expiring_premium, null);
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
                Intent intent = new Intent(HomeActivity.this, UpgradePremiumActivity.class);
                startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }
}
