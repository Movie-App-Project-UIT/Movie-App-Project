package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.WatchingAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.dto.GenreDto;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.utils.NavigationHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pemomovie.utils.FavoriteManager;
import android.view.View;
import android.widget.LinearLayout;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập danh sách phim đang xem
//        RecyclerView rvWatching = findViewById(R.id.rvWatchingMovies);
//        if (rvWatching != null) {
//            rvWatching.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//            rvWatching.setAdapter(new WatchingAdapter());
//        }

        // Set click listener for Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Chuyển sang trang Edit Profile
        ImageView btnEdit = findViewById(R.id.btnEditAvatar);
        if(btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        CardView btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Xử lý nút click mở danh sách yêu thích
        View btnProfileFavorites = findViewById(R.id.btnProfileFavorites);
        if (btnProfileFavorites != null) {
            btnProfileFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, FavoriteActivity.class);
                startActivity(intent);
            });
        }

        // Xử lý nút Nâng cấp Premium
        View btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        if (btnUpgradePremium != null) {
            btnUpgradePremium.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, UpgradePremiumActivity.class);
                startActivity(intent);
            });
        }

        // Xử lý nút Xem tất cả Đang xem
        TextView btnViewAllWatching = findViewById(R.id.btnViewAllWatching);
        if (btnViewAllWatching != null) {
            btnViewAllWatching.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, WatchingActivity.class);
                startActivity(intent);
            });
        }

        // Settings click listeners
        View btnSettingGenres = findViewById(R.id.btnSettingGenres);
        if (btnSettingGenres != null) {
            btnSettingGenres.setOnClickListener(v -> {
                ApiClient.getApiService().getGenres().enqueue(new Callback<List<GenreDto>>() {
                    @Override
                    public void onResponse(Call<List<GenreDto>> call, Response<List<GenreDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GenreDto> genres = response.body();
                            String[] options = new String[genres.size()];
                            for (int i = 0; i < genres.size(); i++) {
                                options[i] = genres.get(i).getName();
                            }
                            if (options.length > 0) {
                                showSettingBottomSheet("Thể loại yêu thích", options, "pref_genres");
                            } else {
                                Toast.makeText(ProfileActivity.this, "Danh sách thể loại trống", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Không thể tải danh sách thể loại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GenreDto>> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        
        View btnSettingLanguage = findViewById(R.id.btnSettingLanguage);
        if (btnSettingLanguage != null) {
            btnSettingLanguage.setOnClickListener(v -> {
                String[] options = {"Tiếng Việt", "English", "日本語 (Nhật Bản)", "한국어 (Hàn Quốc)"};
                showSettingBottomSheet("Ngôn ngữ", options, "pref_language");
            });
        }
        
        View btnSettingQuality = findViewById(R.id.btnSettingQuality);
        if (btnSettingQuality != null) {
            btnSettingQuality.setOnClickListener(v -> {
                String[] options = {"Tự động", "1080p (FHD)", "720p (HD)", "480p", "360p"};
                showSettingBottomSheet("Chất lượng mặc định", options, "pref_quality");
            });
        }
        
        View btnSettingSubtitles = findViewById(R.id.btnSettingSubtitles);
        if (btnSettingSubtitles != null) {
            btnSettingSubtitles.setOnClickListener(v -> {
                String[] options = {"Bật", "Tắt", "Chỉ hiển thị khi không có thuyết minh"};
                showSettingBottomSheet("Phụ đề", options, "pref_subtitles");
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu người dùng mỗi khi màn hình hiển thị (ví dụ: quay lại từ EditProfileActivity)
        loadUserProfile();
        
        // Cập nhật số lượng phim yêu thích
        TextView txtFavCount = findViewById(R.id.txtFavCount);
        if (txtFavCount != null) {
            int favSize = FavoriteManager.getFavorites(this).size();
            txtFavCount.setText(favSize + " phim");
        }
    }

    private void loadUserProfile() {
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileDto profile = response.body();
                    
                    TextView txtUserName = findViewById(R.id.txtUserName);
                    if (txtUserName != null) {
                        String name = profile.getName();
                        if (name == null || name.trim().isEmpty()) {
                            txtUserName.setText("User");
                        } else {
                            txtUserName.setText(name);
                        }
                    }

                    ImageView imgAvatar = findViewById(R.id.imgAvatar);
                    if (imgAvatar != null) {
                        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().trim().isEmpty()) {
                            String avatarUrl = profile.getAvatarUrl().trim();
                            if (avatarUrl.startsWith("\"") && avatarUrl.endsWith("\"")) {
                                avatarUrl = avatarUrl.substring(1, avatarUrl.length() - 1);
                            }
                            int paddingPx = (int) (3 * getResources().getDisplayMetrics().density);
                            imgAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                            imgAvatar.setBackgroundColor(Color.parseColor("#8C8E92"));
                            Glide.with(ProfileActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_avatar)
                                    .circleCrop()
                                    .into(imgAvatar);
                        } else {
                            imgAvatar.setBackgroundColor(Color.parseColor("#A7F3D0"));
                            imgAvatar.setPadding(0, 0, 0, 0);
                            imgAvatar.setImageResource(R.drawable.ic_avatar);
                        }
                    }
// Xử lý hiển thị nút Admin
                    android.view.View layoutAdmin = findViewById(R.id.layoutAdmin);
                    if (layoutAdmin != null) {
                        if ("ADMIN".equals(profile.getRole())) {
                            layoutAdmin.setVisibility(android.view.View.VISIBLE);
                            findViewById(R.id.btnAdminDashboard).setOnClickListener(v -> {
                                Intent intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
                                startActivity(intent);
                            });
                        } else {
                            layoutAdmin.setVisibility(android.view.View.GONE);
                        }
                    }

                    // Logic hiển thị bảng Premium
                    LinearLayout layoutPremium = findViewById(R.id.layoutPremium);
                    LinearLayout layoutUpgradePremium = findViewById(R.id.layoutUpgradePremium);
                    
                    if (profile.isPremium()) {
                        // Đã là premium thì hiện dòng "Thành viên Premium" dưới tên và ẩn bảng Nâng cấp
                        if (layoutPremium != null) layoutPremium.setVisibility(View.VISIBLE);
                        if (layoutUpgradePremium != null) layoutUpgradePremium.setVisibility(View.GONE);
                    } else {
                        // Chưa mua Premium thì ẩn dòng "Thành viên" và hiện bảng "Nâng cấp" to chà bá
                        if (layoutPremium != null) layoutPremium.setVisibility(View.GONE);
                        if (layoutUpgradePremium != null) layoutUpgradePremium.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Không thể tải hồ sơ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showSettingBottomSheet(String title, String[] options, String prefKey) {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.bg_bottom_sheet);
        int padding = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        
        android.widget.TextView tvTitle = new android.widget.TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(android.graphics.Color.WHITE);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, padding);
        layout.addView(tvTitle);
        
        // Get currently selected option
        android.content.SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String currentSelection = prefs.getString(prefKey, options[0]);
        
        for (String option : options) {
            android.widget.TextView tvOption = new android.widget.TextView(this);
            tvOption.setText(option);
            tvOption.setTextSize(16);
            tvOption.setPadding(padding, padding, padding, padding);
            
            // Highlight selected
            if (option.equals(currentSelection)) {
                tvOption.setTextColor(android.graphics.Color.parseColor("#A78BFA")); // Purple accent
                tvOption.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvOption.setTextColor(android.graphics.Color.WHITE);
            }
            
            // Background ripple
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tvOption.setBackgroundResource(outValue.resourceId);
            tvOption.setClickable(true);
            
            tvOption.setOnClickListener(v -> {
                prefs.edit().putString(prefKey, option).apply();
                android.widget.Toast.makeText(this, "Đã chọn: " + option, android.widget.Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            });
            
            layout.addView(tvOption);
            
            // Divider
            android.view.View divider = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (1 * getResources().getDisplayMetrics().density)
            );
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(android.graphics.Color.parseColor("#333333"));
            layout.addView(divider);
        }
        
        bottomSheetDialog.setContentView(layout);
        bottomSheetDialog.show();
    }
}

