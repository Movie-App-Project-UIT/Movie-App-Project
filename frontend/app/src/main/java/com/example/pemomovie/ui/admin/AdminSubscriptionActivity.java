package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminSubscriptionAdapter;
import com.example.pemomovie.mockdata.MockDataHelper;
import com.example.pemomovie.utils.AdminNavigationHelper;

public class AdminSubscriptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_subscription);
        AdminNavigationHelper.setupBottomNavigation(this);

        ImageView btnAddSubscription = findViewById(R.id.btnAddSubscription);
        if (btnAddSubscription != null) {
            btnAddSubscription.setOnClickListener(v -> {
                Toast.makeText(this, "Thêm Gói Premium mới", Toast.LENGTH_SHORT).show();
            });
        }

        RecyclerView rvSubscriptions = findViewById(R.id.rvSubscriptions);
        if (rvSubscriptions != null) {
            rvSubscriptions.setLayoutManager(new LinearLayoutManager(this));
            AdminSubscriptionAdapter adapter = new AdminSubscriptionAdapter(this);
            adapter.setSubscriptions(MockDataHelper.getMockSubscriptions());
            rvSubscriptions.setAdapter(adapter);
        }
    }
}
