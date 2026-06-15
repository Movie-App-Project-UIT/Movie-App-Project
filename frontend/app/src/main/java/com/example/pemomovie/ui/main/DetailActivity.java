package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //tạo màu linear cho IMDb
        TextView txtIMDb = findViewById(R.id.txt_IMDb);
        Shader textShader = new LinearGradient(0, 0, 0, txtIMDb.getTextSize(),
                new int[]{Color.parseColor("#6C29D6"), Color.parseColor("#F43393")}, null, Shader.TileMode.CLAMP);

        txtIMDb.getPaint().setShader(textShader);

        com.example.pemomovie.utils.NavigationHelper.setupBottomNavigation(this);
        
        Long movieId = getIntent().getLongExtra("MOVIE_ID", -1);
        if (movieId != -1) {
            loadMovieDetails(movieId);
        }
    }

    private void loadMovieDetails(Long movieId) {
        com.example.pemomovie.api.ApiClient.getApiService().getMediaDetail(movieId).enqueue(new retrofit2.Callback<com.example.pemomovie.dto.MediaDetailResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.pemomovie.dto.MediaDetailResponse> call, retrofit2.Response<com.example.pemomovie.dto.MediaDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.pemomovie.dto.MediaDetailResponse detail = response.body();
                    bindDataToUi(detail);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.pemomovie.dto.MediaDetailResponse> call, Throwable t) {
                // handle error
            }
        });
    }

    private void bindDataToUi(com.example.pemomovie.dto.MediaDetailResponse detail) {
        android.widget.ImageView ivBackdrop = findViewById(R.id.ivBackdrop);
        TextView txtIMDb = findViewById(R.id.txt_IMDb);
        TextView txtVIP = findViewById(R.id.txt_VIP);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvStartRanking = findViewById(R.id.tvStartRanking);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvView = findViewById(R.id.tvView);
        TextView tvMediaType = findViewById(R.id.tvMediaType);
        TextView tvCountry = findViewById(R.id.tvCountry);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvDesc = findViewById(R.id.tvDesc);

        tvTitle.setText(detail.getTitle());
        tvYear.setText(detail.getReleaseYear() != null ? String.valueOf(detail.getReleaseYear()) : "N/A");
        tvStartRanking.setText(String.format(java.util.Locale.US, "%.1f/10", detail.getVoteAverage()));
        txtIMDb.setText(String.format(java.util.Locale.US, "IMDb %.1f", detail.getVoteAverage()));
        tvDuration.setText(detail.getDuration() != null ? detail.getDuration() + " phút" : "N/A");
        tvView.setText(detail.getViewCount() != null ? detail.getViewCount() + " lượt xem" : "N/A");
        tvMediaType.setText(detail.getGenre());
        tvCountry.setText(detail.getCountry());
        tvGenre.setText(detail.getLanguage());
        tvDesc.setText(detail.getOverview());

        if (detail.isPremium()) {
            txtVIP.setVisibility(android.view.View.VISIBLE);
        } else {
            txtVIP.setVisibility(android.view.View.GONE);
        }

        if (detail.getBackdropUrl() != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(detail.getBackdropUrl())
                    .into(ivBackdrop);
        } else if (detail.getPosterUrl() != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(detail.getPosterUrl())
                    .into(ivBackdrop);
        }
    }
}