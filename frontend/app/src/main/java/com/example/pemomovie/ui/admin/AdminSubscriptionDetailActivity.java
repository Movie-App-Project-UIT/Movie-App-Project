package com.example.pemomovie.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminUserAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminSubscriptionDto;
import com.example.pemomovie.dto.AdminUserDto;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSubscriptionDetailActivity extends AppCompatActivity {

    private EditText edtName, edtDescription, edtPrice, edtDuration;
    private SwitchCompat switchStatus;
    private TextView tvStatusDesc, tvEmptyGift;
    private LinearLayout layoutGiftArea;
    private RecyclerView rvGiftedUsers;
    private AdminUserAdapter userAdapter; // Can reuse AdminUserAdapter if available

    private ApiService apiService;
    private Long subscriptionId = -1L;
    private boolean isDeleted = false; // meaning inactive in this context
    
    private android.widget.CompoundButton.OnCheckedChangeListener switchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_subscription_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        apiService = ApiClient.getClient().create(ApiService.class);

        subscriptionId = getIntent().getLongExtra("SUBSCRIPTION_ID", -1L);
        boolean isActive = getIntent().getBooleanExtra("SUBSCRIPTION_IS_ACTIVE", true);
        isDeleted = !isActive;

        initViews();
        setupListeners();

        if (subscriptionId != -1L) {
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText("Chi tiết Gói Premium");
            layoutGiftArea.setVisibility(View.VISIBLE);
            loadSubscriptionData();
            loadGiftedUsers();
        } else {
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText("Thêm Gói Mới");
            layoutGiftArea.setVisibility(View.GONE);
            findViewById(R.id.layoutStatus).setVisibility(View.GONE);
        }
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtDuration = findViewById(R.id.edtDuration);
        switchStatus = findViewById(R.id.switchStatus);
        tvStatusDesc = findViewById(R.id.tvStatusDesc);
        tvEmptyGift = findViewById(R.id.tvEmptyGift);
        layoutGiftArea = findViewById(R.id.layoutGiftArea);
        rvGiftedUsers = findViewById(R.id.rvGiftedUsers);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // We can create a simple adapter or reuse AdminUserAdapter
        // For now let's assume we have AdminUserAdapter or we can make one.
        rvGiftedUsers.setLayoutManager(new LinearLayoutManager(this));
        // userAdapter = new AdminUserAdapter(this);
        // rvGiftedUsers.setAdapter(userAdapter);

        switchListener = (buttonView, isChecked) -> {
            if (subscriptionId == -1L) return;
            
            // Temporary disable listener to avoid loop
            switchStatus.setOnCheckedChangeListener(null);
            
            apiService.toggleSubscriptionStatus(subscriptionId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isDeleted = !isChecked; // isChecked means Active
                        tvStatusDesc.setText(!isDeleted ? "Gói đang hiển thị cho khách hàng" : "Gói đã bị ẩn và gửi thông báo");
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        switchStatus.setChecked(!isChecked);
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                    switchStatus.setOnCheckedChangeListener(switchListener);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    switchStatus.setChecked(!isChecked);
                    Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    switchStatus.setOnCheckedChangeListener(switchListener);
                }
            });
        };

        if (subscriptionId != -1L) {
            switchStatus.setChecked(!isDeleted);
            tvStatusDesc.setText(!isDeleted ? "Gói đang hiển thị cho khách hàng" : "Gói đã bị ẩn và gửi thông báo");
            switchStatus.setOnCheckedChangeListener(switchListener);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnSave).setOnClickListener(v -> saveSubscription());

        MaterialButton btnGiftSubscription = findViewById(R.id.btnGiftSubscription);
        if (btnGiftSubscription != null) {
            btnGiftSubscription.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminSubscriptionGiftActivity.class);
                intent.putExtra("SUBSCRIPTION_ID", subscriptionId);
                startActivity(intent);
            });
        }
    }

    private void loadSubscriptionData() {
        apiService.getAllSubscriptions().enqueue(new Callback<List<AdminSubscriptionDto>>() {
            @Override
            public void onResponse(Call<List<AdminSubscriptionDto>> call, Response<List<AdminSubscriptionDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (AdminSubscriptionDto dto : response.body()) {
                        if (dto.getId().equals(subscriptionId)) {
                            edtName.setText(dto.getName());
                            edtDescription.setText(dto.getDescription());
                            edtPrice.setText(String.valueOf(dto.getPrice()));
                            edtDuration.setText(String.valueOf(dto.getDurationDays()));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AdminSubscriptionDto>> call, Throwable t) {
                Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGiftedUsers() {
        apiService.getGiftedUsers(subscriptionId).enqueue(new Callback<List<AdminUserDto>>() {
            @Override
            public void onResponse(Call<List<AdminUserDto>> call, Response<List<AdminUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AdminUserDto> users = response.body();
                    if (users.isEmpty()) {
                        tvEmptyGift.setVisibility(View.VISIBLE);
                        rvGiftedUsers.setVisibility(View.GONE);
                    } else {
                        tvEmptyGift.setVisibility(View.GONE);
                        rvGiftedUsers.setVisibility(View.VISIBLE);
                        if (userAdapter == null) {
                            userAdapter = new AdminUserAdapter(AdminSubscriptionDetailActivity.this);
                            rvGiftedUsers.setAdapter(userAdapter);
                        }
                        userAdapter.setUsers(users);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AdminUserDto>> call, Throwable t) {
                Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi khi lấy danh sách user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSubscription() {
        String name = edtName.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String durationStr = edtDuration.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSubscriptionDto dto = new AdminSubscriptionDto();
        dto.setName(name);
        dto.setDescription(desc);
        dto.setPrice(Double.parseDouble(priceStr));
        dto.setDurationDays(Integer.parseInt(durationStr));

        if (subscriptionId == -1L) {
            apiService.createSubscription(dto).enqueue(new Callback<AdminSubscriptionDto>() {
                @Override
                public void onResponse(Call<AdminSubscriptionDto> call, Response<AdminSubscriptionDto> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Tạo gói thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi khi tạo gói", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AdminSubscriptionDto> call, Throwable t) {
                    Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.updateSubscription(subscriptionId, dto).enqueue(new Callback<AdminSubscriptionDto>() {
                @Override
                public void onResponse(Call<AdminSubscriptionDto> call, Response<AdminSubscriptionDto> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AdminSubscriptionDto> call, Throwable t) {
                    Toast.makeText(AdminSubscriptionDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
