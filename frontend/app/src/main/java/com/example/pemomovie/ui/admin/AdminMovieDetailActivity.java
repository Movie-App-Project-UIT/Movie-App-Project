package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminMovieSaveRequest;
import com.example.pemomovie.dto.MediaDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMovieDetailActivity extends AppCompatActivity {

    public static MediaDetailResponse previewData; // For caching preview data

    private EditText etTitle, etOverview, etLanguage, etVideoUrl, etGenre, etCountry, etTmdbId, etVoteAverage;
    private androidx.appcompat.widget.SwitchCompat swPremium, swDeleted;
    private ImageView ivPoster;
    private Button btnSave, btnLoadTmdb, btnViewReviews;
    private ProgressBar progressBar;
    private ApiService apiService;

    private boolean isCreateMode;
    private Integer tmdbId;
    private Long movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_movie_detail);

        apiService = ApiClient.getApiService();

        etTitle = findViewById(R.id.etTitle);
        etOverview = findViewById(R.id.etOverview);
        etLanguage = findViewById(R.id.etLanguage);
        etVideoUrl = findViewById(R.id.etVideoUrl);
        etGenre = findViewById(R.id.etGenre);
        etCountry = findViewById(R.id.etCountry);
        etTmdbId = findViewById(R.id.etTmdbId);
        etVoteAverage = findViewById(R.id.etVoteAverage);
        swPremium = findViewById(R.id.swPremium);
        swDeleted = findViewById(R.id.swDeleted);
        ivPoster = findViewById(R.id.ivPoster);
        btnSave = findViewById(R.id.btnSave);
        btnLoadTmdb = findViewById(R.id.btnLoadTmdb);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        progressBar = findViewById(R.id.progressBar);

        btnLoadTmdb.setOnClickListener(v -> handleLoadTmdb());

        btnViewReviews.setOnClickListener(v -> {
            if (movieId != null && movieId != -1) {
                AdminMovieReviewBottomSheet bottomSheet = new AdminMovieReviewBottomSheet(movieId);
                bottomSheet.show(getSupportFragmentManager(), "AdminMovieReviewBottomSheet");
            } else {
                Toast.makeText(this, "Vui lòng lưu phim trước khi xem bình luận", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        isCreateMode = getIntent().getBooleanExtra("IS_CREATE_MODE", false);
        
        if (isCreateMode) {
            tvHeaderTitle.setText("Tạo Phim Mới");
            btnSave.setText("Xác nhận tạo phim");
            tmdbId = getIntent().getIntExtra("TMDB_ID", -1);
            String videoUrl = getIntent().getStringExtra("VIDEO_URL");

            if (previewData != null) {
                populateUI(previewData, videoUrl);
            } else {
                Toast.makeText(this, "Không có dữ liệu preview", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            tvHeaderTitle.setText("Cập nhật Phim");
            btnSave.setText("Lưu thay đổi");
            movieId = getIntent().getLongExtra("MOVIE_ID", -1);
            
            if (movieId != -1) {
                fetchMovieData(movieId);
            } else {
                Toast.makeText(this, "ID Phim không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        btnSave.setOnClickListener(v -> saveMovie());
    }

    private void handleLoadTmdb() {
        String idStr = etTmdbId.getText().toString().trim();
        if (idStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập TMDB ID", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            tmdbId = Integer.parseInt(idStr);
            loadPreviewFromTmdb(tmdbId);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPreviewFromTmdb(Integer id) {
        progressBar.setVisibility(View.VISIBLE);
        btnLoadTmdb.setEnabled(false);

        apiService.previewTmdbMovie(id).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLoadTmdb.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    populateUI(response.body(), etVideoUrl.getText().toString());
                    Toast.makeText(AdminMovieDetailActivity.this, "Tải dữ liệu thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminMovieDetailActivity.this, "Không thể tải dữ liệu, hãy kiểm tra lại id", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLoadTmdb.setEnabled(true);
                Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(MediaDetailResponse data, String videoUrl) {
        if (data.getTmdbId() != null) {
            etTmdbId.setText(String.valueOf(data.getTmdbId()));
        }
        
        etTitle.setText(data.getTitle() != null ? data.getTitle() : "");
        etOverview.setText(data.getOverview() != null ? data.getOverview() : "");
        etLanguage.setText(data.getLanguage() != null ? data.getLanguage() : "");
        
        if (data.getVoteAverage() != null) {
            etVoteAverage.setText(String.format("%.1f", data.getVoteAverage()));
        } else {
            etVoteAverage.setText("Chưa có");
        }
        
        String genresStr = data.getGenres() != null ? String.join(", ", data.getGenres()) : "Không có dữ liệu";
        etGenre.setText(genresStr);
        etCountry.setText(data.getCountryName() != null ? data.getCountryName() : "Không có dữ liệu");
        
        if (videoUrl != null) {
            etVideoUrl.setText(videoUrl);
        } else if (data.getTrailerUrl() != null) {
            etVideoUrl.setText(data.getTrailerUrl()); // default to trailer if no videoUrl
        }
        
        
        swPremium.setChecked(data.isPremium());
        swDeleted.setChecked(data.isDeleted());

        Glide.with(this)
             .load(data.getPosterUrl())
             .placeholder(R.drawable.bg_poster_rounded)
             .into(ivPoster);
    }

    private void fetchMovieData(Long id) {
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        apiService.getMediaDetailAdmin(id).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    // For update mode, we need TMDB ID for the save request if we don't have it elsewhere?
                    // Actually, update doesn't strictly need TMDB ID because we update by ID, but the DTO expects it.
                    // We can pass null or 0 since backend ignores it on update.
                    populateUI(response.body(), null);
                } else {
                    Toast.makeText(AdminMovieDetailActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMovie() {
        String title = etTitle.getText().toString().trim();
        String overview = etOverview.getText().toString().trim();
        String language = etLanguage.getText().toString().trim();
        String videoUrl = etVideoUrl.getText().toString().trim();
        boolean premium = swPremium.isChecked();
        boolean deleted = swDeleted.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên phim", Toast.LENGTH_SHORT).show();
            return;
        }

        String idStr = etTmdbId.getText().toString().trim();
        int finalTmdbId = 0;
        if (!idStr.isEmpty()) {
            try { finalTmdbId = Integer.parseInt(idStr); } catch (Exception e) {}
        }

        AdminMovieSaveRequest request = new AdminMovieSaveRequest(
                finalTmdbId, 
                videoUrl, 
                premium, 
                deleted,
                title, 
                overview, 
                language
        );

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (isCreateMode) {
            apiService.createMovie(request).enqueue(new Callback<MediaDetailResponse>() {
                @Override
                public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminMovieDetailActivity.this, "Tạo phim thành công!", Toast.LENGTH_SHORT).show();
                        previewData = null; // Clear cache
                        finish();
                    } else {
                        Toast.makeText(AdminMovieDetailActivity.this, "Lỗi tạo phim: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.updateMovie(movieId, request).enqueue(new Callback<MediaDetailResponse>() {
                @Override
                public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminMovieDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminMovieDetailActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
