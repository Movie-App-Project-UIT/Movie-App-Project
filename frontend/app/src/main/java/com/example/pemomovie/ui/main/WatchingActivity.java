package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.example.pemomovie.utils.NavigationHelper;

import com.example.pemomovie.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.adapter.WatchingAdapter;

public class WatchingActivity extends AppCompatActivity {
    private com.example.pemomovie.utils.GlobalHeaderHelper globalHeaderHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_watching);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NavigationHelper.setupBottomNavigation(this);
        globalHeaderHelper = new com.example.pemomovie.utils.GlobalHeaderHelper(this);
        globalHeaderHelper.setupGlobalHeader(findViewById(R.id.globalHeaderInclude));

        RecyclerView rvContinue = findViewById(R.id.rvContinue);
        rvContinue.setLayoutManager(new LinearLayoutManager(this));
        WatchingAdapter watchingAdapter = new WatchingAdapter(true);
        rvContinue.setAdapter(watchingAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadWatchingHistory();
        if (globalHeaderHelper != null) {
            globalHeaderHelper.fetchNotifications();
        }
    }

    private void loadWatchingHistory() {
        com.example.pemomovie.api.ApiClient.getApiService().getUserHistory().enqueue(new retrofit2.Callback<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> call, retrofit2.Response<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto> historyList = response.body();
                    RecyclerView rvContinue = findViewById(R.id.rvContinue);
                    if (rvContinue != null && rvContinue.getAdapter() instanceof WatchingAdapter) {
                        WatchingAdapter adapter = (WatchingAdapter) rvContinue.getAdapter();
                        adapter.setData(historyList);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> call, Throwable t) {
                Toast.makeText(WatchingActivity.this, "Lỗi khi tải lịch sử: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}