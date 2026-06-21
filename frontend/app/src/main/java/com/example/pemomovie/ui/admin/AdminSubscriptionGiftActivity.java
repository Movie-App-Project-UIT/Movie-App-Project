package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminGiftUserAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminUserDto;
import com.example.pemomovie.dto.GiftSubscriptionRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSubscriptionGiftActivity extends AppCompatActivity {

    private RecyclerView rvEligibleUsers;
    private AdminGiftUserAdapter adapter;
    private ApiService apiService;
    private Long subscriptionId;
    private ProgressBar progressBar;
    
    private EditText edtSearch;
    private ChipGroup chipGroupFilter;
    
    private String currentSearchQuery = "";
    private Boolean currentPremiumFilter = null; // null: All, true: Premium, false: Free

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_subscription_gift);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        apiService = ApiClient.getClient().create(ApiService.class);
        subscriptionId = getIntent().getLongExtra("SUBSCRIPTION_ID", -1L);
        if (subscriptionId == -1L) {
            Toast.makeText(this, "Không tìm thấy gói", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadEligibleUsers();
    }

    private void initViews() {
        rvEligibleUsers = findViewById(R.id.rvEligibleUsers);
        rvEligibleUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminGiftUserAdapter(this);
        rvEligibleUsers.setAdapter(adapter);
        
        progressBar = findViewById(R.id.progressBar);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        MaterialButton btnSubmitGift = findViewById(R.id.btnSubmitGift);
        if (btnSubmitGift != null) {
            btnSubmitGift.setOnClickListener(v -> submitGift());
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                loadEligibleUsers();
            }
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentPremiumFilter = null;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) {
                    currentPremiumFilter = null;
                } else if (checkedId == R.id.chipFree) {
                    currentPremiumFilter = false;
                } else if (checkedId == R.id.chipPremium) {
                    currentPremiumFilter = true;
                }
            }
            loadEligibleUsers();
        });
    }

    private void loadEligibleUsers() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getUsers(currentPremiumFilter, currentSearchQuery.isEmpty() ? null : currentSearchQuery)
            .enqueue(new Callback<List<AdminUserDto>>() {
                @Override
                public void onResponse(Call<List<AdminUserDto>> call, Response<List<AdminUserDto>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.setUsers(response.body());
                    } else {
                        Toast.makeText(AdminSubscriptionGiftActivity.this, "Lỗi khi tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<AdminUserDto>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminSubscriptionGiftActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void submitGift() {
        List<Long> selectedIds = adapter.getSelectedUserIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        GiftSubscriptionRequest request = new GiftSubscriptionRequest();
        request.setUserIds(selectedIds);

        progressBar.setVisibility(View.VISIBLE);
        apiService.giftSubscription(subscriptionId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminSubscriptionGiftActivity.this, "Tặng gói thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminSubscriptionGiftActivity.this, "Lỗi khi tặng gói", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminSubscriptionGiftActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
