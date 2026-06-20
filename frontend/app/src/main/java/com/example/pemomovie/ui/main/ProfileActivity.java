package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.utils.NavigationHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pemomovie.utils.FavoriteManager;
import android.view.View;
import android.widget.LinearLayout;

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

        androidx.cardview.widget.CardView btnLogout = findViewById(R.id.btnLogout);
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
}

