package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.utils.NavigationHelper;
import com.example.pemomovie.R;

public class HomeActivity extends AppCompatActivity {

    private ImageView bannerImage;
    private TextView movieTitle;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bannerImage = findViewById(R.id.bannerImage);
        movieTitle = findViewById(R.id.movieTitle);
        handler = new Handler(Looper.getMainLooper());

        NavigationHelper.setupBottomNavigation(this);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnDetail = findViewById(R.id.btnDetail);
        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            startActivity(intent);
        });

    }


}