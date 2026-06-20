package com.example.pemomovie.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMovieAddActivity extends AppCompatActivity {

    private EditText etTmdbId, etVideoUrl;
    private Button btnFetchData;
    private ProgressBar progressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_add);

        apiService = ApiClient.getApiService();

        etTmdbId = findViewById(R.id.etTmdbId);
        etVideoUrl = findViewById(R.id.etVideoUrl);
        btnFetchData = findViewById(R.id.btnFetchData);
        progressBar = findViewById(R.id.progressBar);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnFetchData.setOnClickListener(v -> {
            String tmdbIdStr = etTmdbId.getText().toString().trim();
            String videoUrl = etVideoUrl.getText().toString().trim();

            if (tmdbIdStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập TMDB ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer tmdbId;
            try {
                tmdbId = Integer.parseInt(tmdbIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "TMDB ID không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchPreviewData(tmdbId, videoUrl);
        });
    }

    private void fetchPreviewData(Integer tmdbId, String videoUrl) {
        progressBar.setVisibility(View.VISIBLE);
        btnFetchData.setEnabled(false);

        apiService.previewTmdbMovie(tmdbId).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnFetchData.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // Caching the response into a static variable to pass to the next activity
                    AdminMovieDetailActivity.previewData = response.body();
                    
                    Intent intent = new Intent(AdminMovieAddActivity.this, AdminMovieDetailActivity.class);
                    intent.putExtra("IS_CREATE_MODE", true);
                    intent.putExtra("TMDB_ID", tmdbId);
                    intent.putExtra("VIDEO_URL", videoUrl);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminMovieAddActivity.this, "Không thể tải dữ liệu. Hãy kiểm tra lại ID.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnFetchData.setEnabled(true);
                Toast.makeText(AdminMovieAddActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
