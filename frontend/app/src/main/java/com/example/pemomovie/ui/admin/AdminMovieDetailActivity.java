package com.example.pemomovie.ui.admin;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminSubtitleAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminMovieSaveRequest;
import com.example.pemomovie.dto.MediaDetailResponse;
import com.example.pemomovie.dto.SubtitleDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMovieDetailActivity extends AppCompatActivity {

    public static MediaDetailResponse previewData; // For caching preview data

    private EditText etTitle, etOverview, etLanguage, etVideoUrl, etGenre, etCountry, etTmdbId, etSubtitleLanguage, etVoteAverage;
    private androidx.appcompat.widget.SwitchCompat swPremium, swDeleted;
    private ImageView ivPoster;
    private Button btnSave, btnLoadTmdb, btnUploadVideo, btnUploadSubtitle, btnViewReviews;
    private ProgressBar progressBar;
    private RecyclerView rvSubtitles;

    private AdminSubtitleAdapter subtitleAdapter;
    private ApiService apiService;

    private boolean isCreateMode;
    private Integer tmdbId;
    private Long movieId;

    private ActivityResultLauncher<String> videoPickerLauncher;
    private ActivityResultLauncher<String> subtitlePickerLauncher;

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
        etSubtitleLanguage = findViewById(R.id.etSubtitleLanguage);
        etVoteAverage = findViewById(R.id.etVoteAverage);
        swPremium = findViewById(R.id.swPremium);
        swDeleted = findViewById(R.id.swDeleted);
        ivPoster = findViewById(R.id.ivPoster);
        btnSave = findViewById(R.id.btnSave);
        btnLoadTmdb = findViewById(R.id.btnLoadTmdb);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);
        btnUploadSubtitle = findViewById(R.id.btnUploadSubtitle);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        progressBar = findViewById(R.id.progressBar);
        rvSubtitles = findViewById(R.id.rvSubtitles);

        subtitleAdapter = new AdminSubtitleAdapter(position -> {
            subtitleAdapter.getSubtitles().remove(position);
            subtitleAdapter.notifyDataSetChanged();
        });
        rvSubtitles.setLayoutManager(new LinearLayoutManager(this));
        rvSubtitles.setAdapter(subtitleAdapter);

        setupFilePickers();

        btnLoadTmdb.setOnClickListener(v -> handleLoadTmdb());

        btnViewReviews.setOnClickListener(v -> {
            if (movieId != null && movieId != -1) {
                AdminMovieReviewBottomSheet bottomSheet = new AdminMovieReviewBottomSheet(movieId);
                bottomSheet.show(getSupportFragmentManager(), "AdminMovieReviewBottomSheet");
            } else {
                Toast.makeText(this, "Vui lòng lưu phim trước khi xem bình luận", Toast.LENGTH_SHORT).show();
            }
        });

        btnUploadVideo.setOnClickListener(v -> videoPickerLauncher.launch("video/*"));

        btnUploadSubtitle.setOnClickListener(v -> {
            String lang = etSubtitleLanguage.getText().toString().trim();
            if (lang.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ngôn ngữ (VD: vi, en)", Toast.LENGTH_SHORT).show();
                return;
            }
            subtitlePickerLauncher.launch("*/*");
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

    private void setupFilePickers() {
        videoPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadFile(uri, "video");
            }
        });

        subtitlePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadFile(uri, "subtitle");
            }
        });
    }

    private void uploadFile(Uri uri, String type) {
        File file = getFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Lỗi đọc file từ bộ nhớ", Toast.LENGTH_SHORT).show();
            return;
        }

        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "multipart/form-data";

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnUploadVideo.setEnabled(false);
        btnUploadSubtitle.setEnabled(false);
        Toast.makeText(this, "Đang tải file lên Cloudinary, vui lòng đợi...", Toast.LENGTH_SHORT).show();

        Call<String> call = type.equals("video") ? apiService.uploadVideoAdmin(body) : apiService.uploadSubtitleAdmin(body);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnUploadVideo.setEnabled(true);
                btnUploadSubtitle.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body();
                    if (type.equals("video")) {
                        etVideoUrl.setText(url);
                        Toast.makeText(AdminMovieDetailActivity.this, "Tải Video lên thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        String lang = etSubtitleLanguage.getText().toString().trim();
                        subtitleAdapter.getSubtitles().add(new AdminMovieSaveRequest.AdminSubtitleRequest(lang, url));
                        subtitleAdapter.notifyDataSetChanged();
                        etSubtitleLanguage.setText(""); // Reset
                        Toast.makeText(AdminMovieDetailActivity.this, "Tải Phụ đề lên thành công!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminMovieDetailActivity.this, "Lỗi Upload Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnUploadVideo.setEnabled(true);
                btnUploadSubtitle.setEnabled(true);
                Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "upload_temp";
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            File tempFile = new File(getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

        if (data.getSubtitles() != null) {
            List<AdminMovieSaveRequest.AdminSubtitleRequest> existingSubs = new ArrayList<>();
            for (SubtitleDto sub : data.getSubtitles()) {
                existingSubs.add(new AdminMovieSaveRequest.AdminSubtitleRequest(sub.getLanguage(), sub.getFileUrl()));
            }
            subtitleAdapter.setSubtitles(existingSubs);
        }

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

        List<AdminMovieSaveRequest.AdminSubtitleRequest> currentSubtitles = subtitleAdapter.getSubtitles();

        AdminMovieSaveRequest request = new AdminMovieSaveRequest(
                finalTmdbId, 
                videoUrl, 
                premium, 
                deleted,
                title, 
                overview, 
                language,
                currentSubtitles
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
