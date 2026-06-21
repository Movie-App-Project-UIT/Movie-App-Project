package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminHistoryAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminHistoryDto;
import com.example.pemomovie.utils.AdminNavigationHelper;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHistoryActivity extends AppCompatActivity {
    private AdminHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        AdminNavigationHelper.setupBottomNavigation(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminHistoryAdapter(this);
            rvHistory.setAdapter(adapter);
            loadHistory();
        }
    }

    private void loadHistory() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAdminHistory().enqueue(new Callback<List<AdminHistoryDto>>() {
            @Override
            public void onResponse(Call<List<AdminHistoryDto>> call, Response<List<AdminHistoryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setHistories(response.body());
                } else {
                    Toast.makeText(AdminHistoryActivity.this, "Không thể tải lịch sử", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AdminHistoryDto>> call, Throwable t) {
                Toast.makeText(AdminHistoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
