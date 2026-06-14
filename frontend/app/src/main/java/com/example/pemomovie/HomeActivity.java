package com.example.pemomovie;

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

        addBounceEffect(findViewById(R.id.btnPlay));
        addBounceEffect(findViewById(R.id.btnDetail));

        addBounceEffect(findViewById(R.id.btnHome));
        addBounceEffect(findViewById(R.id.btnGenres));
        addBounceEffect(findViewById(R.id.btnFavorites));
        addBounceEffect(findViewById(R.id.btnWatching));

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnDetail = findViewById(R.id.btnDetail);
        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
            startActivity(intent);
        });

    }

    private void addBounceEffect(View button) {
        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                v.setSelected(true);
            }
            return false;
        });
    }

}