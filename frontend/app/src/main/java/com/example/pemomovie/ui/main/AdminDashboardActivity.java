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

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Set click listener for Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Setup click listeners for Dashboard items
        android.view.View btnManageMovies = findViewById(R.id.btnManageMovies);
        android.view.View btnManageCategories = findViewById(R.id.btnManageCategories);

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

        // Setup bottom navigation
        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);
    }
}
