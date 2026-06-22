package com.example.pemomovie.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.stream.Collectors;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminUserReviewAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminUserDetailDto;
import com.google.android.material.switchmaterial.SwitchMaterial;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserDetailActivity extends AppCompatActivity {
    private Long userId;
    private AdminUserDetailDto currentUser;
    
    private ImageView imgAvatar, btnBack;
    private TextView txtUserName, txtUserEmail, txtTier, txtStatus;
    private View cardPremiumInfo;
    private TextView txtPlanName, txtPlanEndDate, txtNoReviews;
    private SwitchMaterial switchStatus;
    private CheckBox cbFilterReported;
    private RecyclerView rvReviews;
    private AdminUserReviewAdapter reviewAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        initViews();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        txtTier = findViewById(R.id.txtTier);
        txtStatus = findViewById(R.id.txtStatus);
        
        cardPremiumInfo = findViewById(R.id.cardPremiumInfo);
        txtPlanName = findViewById(R.id.txtPlanName);
        txtPlanEndDate = findViewById(R.id.txtPlanEndDate);
        
        txtNoReviews = findViewById(R.id.txtNoReviews);
        rvReviews = findViewById(R.id.rvReviews);
        switchStatus = findViewById(R.id.switchStatus);
        cbFilterReported = findViewById(R.id.cbFilterReported);

        btnBack.setOnClickListener(v -> finish());
        
        cbFilterReported.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyReviewFilter();
        });

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new AdminUserReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        switchStatus.setOnClickListener(v -> {
            boolean isChecked = switchStatus.isChecked();
            // Hoàn tác lại UI trước khi API gọi xong để tránh sai lệch
            switchStatus.setChecked(!isChecked);
            showConfirmDialog(isChecked);
        });
    }

    private void loadData() {
        apiService.getUserDetails(userId).enqueue(new Callback<AdminUserDetailDto>() {
            @Override
            public void onResponse(Call<AdminUserDetailDto> call, Response<AdminUserDetailDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    updateUI();
                } else {
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminUserDetailDto> call, Throwable t) {
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        txtUserName.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "No name");
        txtUserEmail.setText(currentUser.getEmail());
        
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(currentUser.getAvatarUrl()).into(imgAvatar);
        }

        if ("PREMIUM".equals(currentUser.getTier())) {
            txtTier.setText("PREMIUM");
            txtTier.setTextColor(Color.parseColor("#F59E0B"));
            txtTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4DF59E0B")));
            
            if (currentUser.getCurrentPlanName() != null) {
                cardPremiumInfo.setVisibility(View.VISIBLE);
                txtPlanName.setText(currentUser.getCurrentPlanName());
                txtPlanEndDate.setText(currentUser.getPlanEndDate());
            } else {
                cardPremiumInfo.setVisibility(View.GONE);
            }
        } else {
            txtTier.setText("FREE");
            txtTier.setTextColor(Color.parseColor("#9CA3AF"));
            txtTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4D9CA3AF")));
            cardPremiumInfo.setVisibility(View.GONE);
        }
        
        switchStatus.setChecked(currentUser.isActive());
        updateStatusLabel();

        if (currentUser.getReviews() != null && !currentUser.getReviews().isEmpty()) {
            txtNoReviews.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
            applyReviewFilter();
        } else {
            txtNoReviews.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
        }
    }
    
    private void applyReviewFilter() {
        if (currentUser == null || currentUser.getReviews() == null) return;
        java.util.List<AdminUserDetailDto.ReviewDto> filteredList = new java.util.ArrayList<>(currentUser.getReviews());
        if (cbFilterReported.isChecked()) {
            filteredList = filteredList.stream()
                .filter(r -> r.getReportCount() > 0)
                .sorted((r1, r2) -> Long.compare(r2.getReportCount(), r1.getReportCount()))
                .collect(Collectors.toList());
        }
        reviewAdapter.setReviews(filteredList);
    }
    
    private void updateStatusLabel() {
        if (currentUser.isActive()) {
            txtStatus.setText("Hoạt động");
            txtStatus.setTextColor(Color.parseColor("#10B981"));
            txtStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3310B981")));
        } else {
            txtStatus.setText("Vô hiệu hóa");
            txtStatus.setTextColor(Color.parseColor("#EF4444"));
            txtStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33EF4444")));
        }
    }

    private void showConfirmDialog(boolean willBeActive) {
        String action = willBeActive ? "kích hoạt" : "vô hiệu hóa";
        
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        
        tvTitle.setText("Xác nhận");
        tvMessage.setText("Bạn có chắc chắn muốn " + action + " tài khoản này?");
        
        if (!willBeActive) {
            icon.setImageResource(R.drawable.ic_delete); // Vô hiệu hóa
            icon.setColorFilter(Color.parseColor("#EF4444"));
        } else {
            icon.setImageResource(R.drawable.ic_restore); // Kích hoạt lại
            icon.setColorFilter(Color.parseColor("#10B981"));
        }
        
        dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            toggleUserStatus();
        });
        
        dialog.show();
    }

    private void toggleUserStatus() {
        apiService.toggleUserStatus(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    currentUser.setActive(!currentUser.isActive());
                    switchStatus.setChecked(currentUser.isActive());
                    updateStatusLabel();
                    Toast.makeText(AdminUserDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminUserDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
