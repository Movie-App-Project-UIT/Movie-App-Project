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
        View layoutNotificationList = popupView.findViewById(R.id.layoutNotificationList);
        View layoutNotifAdminGift = popupView.findViewById(R.id.layoutNotifAdminGift);
        View layoutNotifSuccess = popupView.findViewById(R.id.layoutNotifSuccess);

        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        boolean hasPremiumNotif = prefs.getBoolean("has_new_premium_notification", false);

        if (hasPremiumNotif) {
            if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.GONE);
            if (layoutNotificationList != null) layoutNotificationList.setVisibility(View.VISIBLE);
            if (layoutNotifAdminGift != null) layoutNotifAdminGift.setVisibility(View.GONE);
            if (layoutNotifSuccess != null) layoutNotifSuccess.setVisibility(View.VISIBLE);
        } else {
            if (layoutEmptyNotification != null) layoutEmptyNotification.setVisibility(View.VISIBLE);
            if (layoutNotificationList != null) layoutNotificationList.setVisibility(View.GONE);
        }

        if (layoutNotifAdminGift != null) {
            layoutNotifAdminGift.setOnClickListener(v -> {
                popupWindow.dismiss();
                Toast.makeText(HomeActivity.this, "Đã kích hoạt gói Premium quà tặng thành công!", Toast.LENGTH_LONG).show();
            });
        }

        if (layoutNotifSuccess != null) {
            layoutNotifSuccess.setOnClickListener(v -> {
                popupWindow.dismiss();
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Đánh dấu đã đọc
        View tvMarkAsRead = popupView.findViewById(R.id.tvMarkAsRead);
        if (tvMarkAsRead != null) {
            tvMarkAsRead.setOnClickListener(v -> {
                prefs.edit().putBoolean("has_new_premium_notification", false).apply();
                popupWindow.dismiss();
                Toast.makeText(HomeActivity.this, "Đã đánh dấu đọc tất cả", Toast.LENGTH_SHORT).show();
            });
        }

        int xoff = anchorView.getWidth() - width;
        int yoff = (int) (8 * getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchorView, xoff, yoff);
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
}
