package com.example.pemomovie.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.NotificationAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.NotificationDto;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.pemomovie.dto.UserProfileDto;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationDto> notificationList = new ArrayList<>();
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        apiService = ApiClient.getApiService();

        rvNotifications = findViewById(R.id.rvNotifications);
        progressBar = findViewById(R.id.progressBar);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(NotificationDto notification) {
                // Do something when item clicked
            }

            @Override
            public void onActionClick(NotificationDto notification) {
                if ("GIFT_RECEIVED".equals(notification.getType())) {
                    // Hiển thị bottom sheet
                    showAdminGiftConfirmationDialog(notification);
                } else if ("SUBSCRIPTION_EXPIRING".equals(notification.getType())) {
                    showExpiringPremiumBottomSheet(notification);
                } else if ("SUBSCRIPTION_NEW_PLAN".equals(notification.getType())) {
                    Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        });
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.tvMarkAllRead).setOnClickListener(v -> {
            // Đánh dấu đã đọc tất cả
            for (NotificationDto notif : notificationList) {
                if (notif.getRead() == null || !notif.getRead()) {
                    notif.setRead(true);
                    markNotificationAsRead(notif.getId());
                }
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã đánh dấu đọc tất cả", Toast.LENGTH_SHORT).show();
        });

        View btnUpgradePremium = findViewById(R.id.btnUpgradePremium);
        if (btnUpgradePremium != null) {
            btnUpgradePremium.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationActivity.this, UpgradePremiumActivity.class);
                startActivity(intent);
            });
        }

        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        // Fetch user profile first to get the user ID
        apiService.getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileDto profile = response.body();
                    
                    View layoutUpgradePremium = findViewById(R.id.layoutUpgradePremium);
                    if (layoutUpgradePremium != null) {
                        layoutUpgradePremium.setVisibility(profile.isPremium() ? View.GONE : View.VISIBLE);
                    }

                    try {
                        Long userId = Long.parseLong(profile.getId());
                        
                        // Now fetch notifications
                        apiService.getUserNotifications(userId).enqueue(new Callback<List<NotificationDto>>() {
                            @Override
                            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> response) {
                                progressBar.setVisibility(View.GONE);
                                if (response.isSuccessful() && response.body() != null) {
                                    notificationList.clear();
                                    notificationList.addAll(response.body());
                                    if (notificationList.isEmpty()) {
                                        loadMockData(); // Nếu rỗng thì load mock cho có giao diện
                                    } else {
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {
                                    loadMockData();
                                }
                            }

                            @Override
                            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {
                                progressBar.setVisibility(View.GONE);
                                loadMockData();
                            }
                        });
                    } catch (NumberFormatException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NotificationActivity.this, "Lỗi định dạng User ID", Toast.LENGTH_SHORT).show();
                        loadMockData();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(NotificationActivity.this, "Không thể xác định User ID", Toast.LENGTH_SHORT).show();
                    loadMockData();
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                loadMockData();
            }
        });
    }

    private void loadMockData() {
        notificationList.clear();
        
        NotificationDto n1 = new NotificationDto();
        n1.setId(1L);
        n1.setTitle("Premium sắp hết hạn");
        n1.setMessage("Gói Premium của bạn sẽ hết hạn sau 3 ngày (19/06/2026). Gia hạn ngay để tiếp tục trải nghiệm!");
        n1.setType("SUBSCRIPTION_EXPIRING");
        n1.setRead(false);
        
        NotificationDto n2 = new NotificationDto();
        n2.setId(2L);
        n2.setTitle("Nâng cấp thành công");
        n2.setMessage("Chào mừng bạn đến với Premium! Bạn đã mở khóa tất cả đặc quyền xem phim.");
        n2.setType("SUBSCRIPTION_NEW_PLAN");
        n2.setRead(false);
        
        NotificationDto n3 = new NotificationDto();
        n3.setId(3L);
        n3.setTitle("Quà tặng từ Admin");
        n3.setMessage("Bạn nhận được 7 ngày Premium miễn phí từ Admin. Nhấn để kích hoạt ngay!");
        n3.setType("GIFT_RECEIVED");
        n3.setRead(false);
        
        NotificationDto n4 = new NotificationDto();
        n4.setId(4L);
        n4.setTitle("Phim mới cập nhật");
        n4.setMessage("“Violet Evergarden: Tập đặc biệt” đã có mặt trên PemoMovie. Xem ngay!");
        n4.setType("SYSTEM");
        n4.setRead(false);

        notificationList.add(n1);
        notificationList.add(n2);
        notificationList.add(n3);
        notificationList.add(n4);
        
        adapter.notifyDataSetChanged();
    }

    private void markNotificationAsRead(Long id) {
        if (id == null) return;
        apiService.markNotificationAsRead(id).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {}
        });
    }

    private void showAdminGiftConfirmationDialog(NotificationDto notification) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_activate_gift, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnShowListener(dialog -> {
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        View btnCloseBottomSheet = bottomSheetView.findViewById(R.id.btnCloseBottomSheet);
        if (btnCloseBottomSheet != null) {
            btnCloseBottomSheet.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        View btnLater = bottomSheetView.findViewById(R.id.btnLater);
        if (btnLater != null) {
            btnLater.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        View btnConfirmActivate = bottomSheetView.findViewById(R.id.btnConfirmActivate);
        if (btnConfirmActivate != null) {
            btnConfirmActivate.setOnClickListener(v -> {
                progressBar.setVisibility(View.VISIBLE);
                bottomSheetDialog.dismiss();

                // Luồng gọi API thật
                apiService.claimGift(notification.getId()).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            String message = response.body().get("message");
                            if (message == null) message = "Đã kích hoạt gói Premium quà tặng thành công!";
                            Toast.makeText(NotificationActivity.this, message, Toast.LENGTH_LONG).show();
                            
                            notification.setRead(true);
                            adapter.notifyDataSetChanged();
                            
                            // Tải lại profile để cập nhật banner (ẩn đi nếu đã có premium)
                            loadNotifications();
                        } else {
                            Toast.makeText(NotificationActivity.this, "Kích hoạt thất bại. Quà tặng có thể đã được sử dụng.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(NotificationActivity.this, "Lỗi kết nối khi kích hoạt quà tặng", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        bottomSheetDialog.show();
    }

    private void showExpiringPremiumBottomSheet(NotificationDto notification) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_expiring_premium, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnShowListener(dialog -> {
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        TextView tvExpiringSubtitle = bottomSheetView.findViewById(R.id.tvExpiringSubtitle);
        if (tvExpiringSubtitle != null && notification != null && notification.getMessage() != null) {
            tvExpiringSubtitle.setText(notification.getMessage());
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
                Intent intent = new Intent(NotificationActivity.this, UpgradePremiumActivity.class);
                startActivity(intent);
            });
        }

        bottomSheetDialog.show();
    }
}
