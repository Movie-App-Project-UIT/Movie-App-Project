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

import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.WatchingAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.utils.NavigationHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        ImageView btnEdit = findViewById(R.id.btnEditAvatar);
        if(btnEdit != null)
        {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }
        NavigationHelper.setupBottomNavigation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu người dùng mỗi khi màn hình hiển thị (ví dụ: quay lại từ EditProfileActivity)
        loadUserProfile();
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
                        // Nếu tên bị null hoặc rỗng, mặc định hiển thị là "User"
                        if (name == null || name.trim().isEmpty()) {
                            txtUserName.setText("User");
                        } else {
                            txtUserName.setText(name);
                        }
                    }

                    ImageView imgAvatar = findViewById(R.id.imgAvatar);
                    if (imgAvatar != null && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(profile.getAvatarUrl())
                                .placeholder(R.drawable.ic_avatar)
                                .into(imgAvatar);
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
