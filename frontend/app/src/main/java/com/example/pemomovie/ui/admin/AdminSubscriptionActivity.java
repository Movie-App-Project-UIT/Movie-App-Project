package com.example.pemomovie.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminSubscriptionAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminSubscriptionDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminSubscriptionActivity extends AppCompatActivity {

    private RecyclerView rvSubscriptions;
    private AdminSubscriptionAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_subscription);

        // Adjust window insets for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        apiService = ApiClient.getClient().create(ApiService.class);

        rvSubscriptions = findViewById(R.id.rvSubscriptions);
        rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSubscriptionAdapter(this);
        rvSubscriptions.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageView btnAddSubscription = findViewById(R.id.btnAddSubscription);
        if (btnAddSubscription != null) {
            btnAddSubscription.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminSubscriptionDetailActivity.class);
                startActivity(intent);
            });
        }

        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSubscriptions();
    }

    private void loadSubscriptions() {
        apiService.getAllSubscriptions().enqueue(new Callback<List<AdminSubscriptionDto>>() {
            @Override
            public void onResponse(Call<List<AdminSubscriptionDto>> call, Response<List<AdminSubscriptionDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setSubscriptions(response.body());
                } else {
                    Toast.makeText(AdminSubscriptionActivity.this, "Lỗi khi tải danh sách gói", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AdminSubscriptionDto>> call, Throwable t) {
                Toast.makeText(AdminSubscriptionActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
