package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminTopMovieAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminDashboardStatsDto;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {
    
    private RecyclerView rvTopTrending;
    private RecyclerView rvTopLiked;
    private AdminTopMovieAdapter trendingAdapter;
    private AdminTopMovieAdapter likedAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Set click listener for Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup click listeners for Dashboard items
        android.view.View btnManageMovies = findViewById(R.id.btnManageMovies);
        android.view.View btnManageCategories = findViewById(R.id.btnManageCategories);
        android.view.View btnManageSubscriptions = findViewById(R.id.btnManageSubscriptions);
        android.view.View btnManageUsers = findViewById(R.id.btnManageUsers);
        android.view.View btnManageHistory = findViewById(R.id.btnManageHistory);

        if (btnManageMovies != null) {
            btnManageMovies.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminMovieActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageCategories != null) {
            btnManageCategories.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminCategoryActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageSubscriptions != null) {
            btnManageSubscriptions.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminSubscriptionActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminUserActivity.class);
                startActivity(intent);
            });
        }

        if (btnManageHistory != null) {
            btnManageHistory.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminHistoryActivity.class);
                startActivity(intent);
            });
        }


        // Setup bottom navigation
        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);
        
        ImageView btnProfileUtility = findViewById(R.id.btnProfileUtility);
        if (btnProfileUtility != null) {
            btnProfileUtility.setOnClickListener(v -> showProfileDropdown(v));
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        rvTopTrending = findViewById(R.id.rvTopTrending);
        rvTopLiked = findViewById(R.id.rvTopLiked);

        rvTopTrending.setLayoutManager(new LinearLayoutManager(this));
        rvTopLiked.setLayoutManager(new LinearLayoutManager(this));

        trendingAdapter = new AdminTopMovieAdapter(this, new ArrayList<>(), true);
        likedAdapter = new AdminTopMovieAdapter(this, new ArrayList<>(), false);

        rvTopTrending.setAdapter(trendingAdapter);
        rvTopLiked.setAdapter(likedAdapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadHomeProfileAvatar();
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        apiService.getDashboardStats().enqueue(new retrofit2.Callback<AdminDashboardStatsDto>() {
            @Override
            public void onResponse(retrofit2.Call<AdminDashboardStatsDto> call, retrofit2.Response<AdminDashboardStatsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    trendingAdapter.updateData(response.body().getTopTrendingMovies());
                    likedAdapter.updateData(response.body().getTopLikedMovies());
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Không thể tải dữ liệu thống kê", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AdminDashboardStatsDto> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHomeProfileAvatar() {
        ImageView btnProfileUtility = findViewById(R.id.btnProfileUtility);
        
        if (btnProfileUtility != null) {
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                int paddingPx = (int) (2 * getResources().getDisplayMetrics().density);
                btnProfileUtility.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                btnProfileUtility.setBackgroundResource(R.drawable.bg_circle_avatar_border);
                com.bumptech.glide.Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(btnProfileUtility);
            } else {
                btnProfileUtility.setBackground(null);
                btnProfileUtility.setPadding(0, 0, 0, 0);
                btnProfileUtility.setImageResource(R.drawable.ic_avatar);
            }
        }
    }

    private void showProfileDropdown(android.view.View anchorView) {
        android.view.LayoutInflater inflater = (android.view.LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        android.view.View popupView = inflater.inflate(R.layout.layout_profile_dropdown, null);

        int width = (int) (240 * getResources().getDisplayMetrics().density);
        int height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(popupView, width, height, focusable);
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(10);
        }

        ImageView ivDropdownAvatar = popupView.findViewById(R.id.ivDropdownAvatar);
        android.widget.TextView tvDropdownName = popupView.findViewById(R.id.tvDropdownName);
        android.widget.TextView tvDropdownEmail = popupView.findViewById(R.id.tvDropdownEmail);

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            if (name != null && !name.isEmpty()) {
                tvDropdownName.setText(name);
            } else {
                tvDropdownName.setText("Admin");
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
                com.bumptech.glide.Glide.with(this)
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

        android.widget.LinearLayout layoutBtnProfile = popupView.findViewById(R.id.layoutBtnProfile);
        android.widget.LinearLayout layoutBtnLogout = popupView.findViewById(R.id.layoutBtnLogout);

        layoutBtnProfile.setOnClickListener(v -> {
            popupWindow.dismiss();
            Intent intent = new Intent(this, com.example.pemomovie.ui.admin.AdminProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        layoutBtnLogout.setOnClickListener(v -> {
            popupWindow.dismiss();
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, com.example.pemomovie.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        int yoff = (int) (8 * getResources().getDisplayMetrics().density);
        popupWindow.showAsDropDown(anchorView, 0, yoff);
    }
}
