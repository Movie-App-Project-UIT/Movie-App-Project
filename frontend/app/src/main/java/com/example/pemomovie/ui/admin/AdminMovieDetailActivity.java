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
import com.example.pemomovie.dto.EpisodeDto;
import com.example.pemomovie.dto.AdminEpisodeSaveRequest;
import com.example.pemomovie.adapter.AdminEpisodeAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMovieDetailActivity extends AppCompatActivity {

    public static MediaDetailResponse previewData; // For caching preview data

    private EditText etTitle, etOverview, etLanguage, etVideoUrl, etGenre, etCountry, etTmdbId, etSubtitleLanguage, etVoteAverage, etExpectedEpisodes;
    private androidx.appcompat.widget.SwitchCompat swPremium, swDeleted;
    private ImageView ivPoster;
    private Button btnSave, btnLoadTmdb, btnUploadVideo, btnUploadSubtitle, btnViewReviews, btnEditExpectedEpisodes;
    private ProgressBar progressBar;
    private RecyclerView rvSubtitles, rvEpisodesAdmin;
    private android.widget.LinearLayout layoutEpisodes, layoutEpisodeDetails;
    private android.widget.RadioGroup rgMediaType;
    private android.widget.RadioButton rbMovie, rbTvSeries;

    private AdminSubtitleAdapter subtitleAdapter;
    private AdminEpisodeAdapter episodeAdapter;
    private List<EpisodeDto> episodeList = new ArrayList<>();
    private Long currentEditingEpisodeId = null;
    private boolean isEditingEpisode = false;
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
        etExpectedEpisodes = findViewById(R.id.etExpectedEpisodes);
        layoutEpisodes = findViewById(R.id.layoutEpisodes);
        layoutEpisodeDetails = findViewById(R.id.layoutEpisodeDetails);
        rvEpisodesAdmin = findViewById(R.id.rvEpisodesAdmin);
        btnEditExpectedEpisodes = findViewById(R.id.btnEditExpectedEpisodes);
        rgMediaType = findViewById(R.id.rgMediaType);
        rbMovie = findViewById(R.id.rbMovie);
        rbTvSeries = findViewById(R.id.rbTvSeries);

        rgMediaType.setOnCheckedChangeListener((group, checkedId) -> handleMediaTypeChange(group, checkedId));

        subtitleAdapter = new AdminSubtitleAdapter(position -> {
            subtitleAdapter.getSubtitles().remove(position);
            subtitleAdapter.notifyDataSetChanged();
        });
        rvSubtitles.setLayoutManager(new LinearLayoutManager(this));
        rvSubtitles.setAdapter(subtitleAdapter);

        episodeAdapter = new AdminEpisodeAdapter(this, episodeList, (episode, position) -> {
            saveCurrentEpisodeDataToLocal();
            loadEpisodeData(episode);
        });
        rvEpisodesAdmin.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
        rvEpisodesAdmin.setAdapter(episodeAdapter);

        btnEditExpectedEpisodes.setOnClickListener(v -> {
            if (rbMovie.isChecked()) {
                Toast.makeText(this, "Chỉ áp dụng cho Phim bộ", Toast.LENGTH_SHORT).show();
                return;
            }
            saveCurrentEpisodeDataToLocal();

            android.app.Dialog inputDialog = new android.app.Dialog(this);
            inputDialog.setContentView(R.layout.dialog_input);
            inputDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            android.widget.TextView tvTitle = inputDialog.findViewById(R.id.tvDialogTitle);
            android.widget.EditText input = inputDialog.findViewById(R.id.etDialogInput);
            tvTitle.setText("Sửa số tập dự kiến");
            input.setText(etExpectedEpisodes.getText().toString());
            
            inputDialog.findViewById(R.id.btnDialogCancel).setOnClickListener(vDialog -> inputDialog.dismiss());
            inputDialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(vDialog -> {
                try {
                    int parsedExpected = Integer.parseInt(input.getText().toString().trim());
                    final int newExpected = Math.max(1, parsedExpected);
                    
                    int currentListSize = episodeList.size();
                    if (newExpected > currentListSize) {
                        for (int i = currentListSize; i < newExpected; i++) {
                            EpisodeDto newEp = new EpisodeDto();
                            newEp.setEpisodeNumber(i + 1);
                            newEp.setDeleted(true); // Trạng thái ẩn
                            episodeList.add(newEp);
                        }
                        etExpectedEpisodes.setText(String.valueOf(newExpected));
                        episodeAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Đã thêm các tập mới ở trạng thái ẩn", Toast.LENGTH_SHORT).show();
                    } else if (newExpected < currentListSize) {
                        // Check for data loss
                        boolean hasData = false;
                        for (int i = newExpected; i < currentListSize; i++) {
                            EpisodeDto ep = episodeList.get(i);
                            if ((ep.getVideoUrl() != null && !ep.getVideoUrl().trim().isEmpty()) || 
                                (ep.getSubtitles() != null && !ep.getSubtitles().isEmpty())) {
                                hasData = true;
                                break;
                            }
                        }

                        if (hasData) {
                            android.app.Dialog warningDialog = new android.app.Dialog(this);
                            warningDialog.setContentView(R.layout.dialog_confirm);
                            warningDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            
                            android.widget.TextView tvWarnTitle = warningDialog.findViewById(R.id.tvDialogTitle);
                            android.widget.TextView tvWarnMessage = warningDialog.findViewById(R.id.tvDialogMessage);
                            tvWarnTitle.setText("Cảnh báo mất dữ liệu");
                            tvWarnMessage.setText("Giảm số tập dự kiến sẽ làm xóa hoàn toàn dữ liệu của các tập thừa. Bạn có chắc chắn muốn tiếp tục?");
                            
                            warningDialog.findViewById(R.id.btnDialogCancel).setOnClickListener(vWarnDialog -> warningDialog.dismiss());
                            warningDialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(vWarnDialog -> {
                                while (episodeList.size() > newExpected) {
                                    episodeList.remove(episodeList.size() - 1);
                                }
                                etExpectedEpisodes.setText(String.valueOf(newExpected));
                                episodeAdapter.notifyDataSetChanged();
                                adjustEpisodeSelectionAfterDeletion();
                                warningDialog.dismiss();
                            });
                            warningDialog.show();
                        } else {
                            while (episodeList.size() > newExpected) {
                                episodeList.remove(episodeList.size() - 1);
                            }
                            etExpectedEpisodes.setText(String.valueOf(newExpected));
                            episodeAdapter.notifyDataSetChanged();
                            adjustEpisodeSelectionAfterDeletion();
                        }
                    } else {
                        etExpectedEpisodes.setText(String.valueOf(newExpected));
                    }
                    inputDialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số tập không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
            inputDialog.show();
        });

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

    private void handleMediaTypeChange(android.widget.RadioGroup group, int checkedId) {
        if (previewData != null) {
            if (checkedId == R.id.rbMovie && "TV_SERIES".equals(previewData.getMediaType()) && !isCreateMode && episodeList.size() > 1) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Cảnh báo")
                        .setMessage("Đổi sang Phim lẻ sẽ làm ẩn đi dữ liệu của các tập khác, chỉ giữ lại tập 1. Bạn có chắc chắn muốn tiếp tục?")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            previewData.setMediaType("MOVIE");
                            populateUI(previewData, etVideoUrl.getText().toString());
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            rgMediaType.setOnCheckedChangeListener(null);
                            rbTvSeries.setChecked(true);
                            rgMediaType.setOnCheckedChangeListener(this::handleMediaTypeChange);
                        })
                        .show();
                return;
            }
            previewData.setMediaType(checkedId == R.id.rbTvSeries ? "TV_SERIES" : "MOVIE");
            populateUI(previewData, etVideoUrl.getText().toString());
        }
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
        File file = getFileFromUri(uri, type);
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

        Call<ResponseBody> call = type.equals("video") ? apiService.uploadVideoAdmin(body) : apiService.uploadSubtitleAdmin(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnUploadVideo.setEnabled(true);
                btnUploadSubtitle.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String url = response.body().string();
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
                    } catch (java.io.IOException e) {
                        Toast.makeText(AdminMovieDetailActivity.this, "Lỗi đọc dữ liệu Server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminMovieDetailActivity.this, "Lỗi Upload Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                btnUploadVideo.setEnabled(true);
                btnUploadSubtitle.setEnabled(true);
                Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getFileFromUri(Uri uri, String type) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = "upload_temp_" + System.currentTimeMillis();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
            
            // Backend validation requires specific extensions
            if (type.equals("video") && !fileName.matches(".*\\.(mp4|mkv|avi)$")) {
                fileName += ".mp4";
            } else if (type.equals("subtitle") && !fileName.matches(".*\\.(srt|vtt)$")) {
                fileName += ".srt";
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
            String type = rbTvSeries.isChecked() ? "TV_SERIES" : "MOVIE";
            loadPreviewFromTmdb(tmdbId, type);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "ID không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPreviewFromTmdb(Integer id, String type) {
        progressBar.setVisibility(View.VISIBLE);
        btnLoadTmdb.setEnabled(false);

        apiService.previewTmdbMovie(id, type).enqueue(new Callback<MediaDetailResponse>() {
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
        if (data == null) return;
        previewData = data;

        rgMediaType.setOnCheckedChangeListener(null);
        if ("TV_SERIES".equals(data.getMediaType())) {
            rbTvSeries.setChecked(true);
        } else {
            rbMovie.setChecked(true);
        }
        rgMediaType.setOnCheckedChangeListener(this::handleMediaTypeChange);

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

        String genresStr = data.getGenres() != null ? String.join(", ", data.getGenres()).replace("Phim ", "").replace("phim ", "") : "Không có dữ liệu";
        etGenre.setText(genresStr);
        etCountry.setText(data.getCountryName() != null ? data.getCountryName() : "Không có dữ liệu");

        if (data.getExpectedEpisodes() != null) {
            etExpectedEpisodes.setText(String.valueOf(data.getExpectedEpisodes()));
        } else {
            etExpectedEpisodes.setText("1");
        }

        if ("TV_SERIES".equals(data.getMediaType())) {
            etExpectedEpisodes.setVisibility(View.VISIBLE);
            btnEditExpectedEpisodes.setVisibility(View.VISIBLE);
            findViewById(R.id.tvExpectedEpisodesLabel).setVisibility(View.VISIBLE);
        } else {
            etExpectedEpisodes.setVisibility(View.GONE);
            btnEditExpectedEpisodes.setVisibility(View.GONE);
            findViewById(R.id.tvExpectedEpisodesLabel).setVisibility(View.GONE);
        }

        if ("TV_SERIES".equals(data.getMediaType()) && !isCreateMode) {
            layoutEpisodes.setVisibility(View.VISIBLE);
            episodeList.clear();
            if (data.getSeasons() != null && !data.getSeasons().isEmpty()) {
                com.example.pemomovie.dto.SeasonDto season = data.getSeasons().get(0);
                if (season.getEpisodes() != null) {
                    episodeList.addAll(season.getEpisodes());
                }
            }
            episodeAdapter.notifyDataSetChanged();
            if (!episodeList.isEmpty()) {
                episodeAdapter.setSelectedPosition(0);
                loadEpisodeData(episodeList.get(0));
            } else {
                isEditingEpisode = false;
                currentEditingEpisodeId = null;
                layoutEpisodeDetails.setVisibility(View.GONE);
            }
        } else {
            layoutEpisodes.setVisibility(View.GONE);
        }

        if ("TV_SERIES".equals(data.getMediaType()) && isCreateMode) {
            layoutEpisodeDetails.setVisibility(View.GONE);
        } else if ("MOVIE".equals(data.getMediaType()) || isEditingEpisode) {
            layoutEpisodeDetails.setVisibility(View.VISIBLE);
        }

        if (!isEditingEpisode && !"TV_SERIES".equals(data.getMediaType())) {
            if (videoUrl != null) {
                etVideoUrl.setText(videoUrl);
            } else if (data.getTrailerUrl() != null) {
                etVideoUrl.setText(data.getTrailerUrl()); 
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
        }

        Glide.with(this)
             .load(data.getPosterUrl())
             .placeholder(R.drawable.bg_poster_rounded)
             .into(ivPoster);
    }

    private void loadEpisodeData(EpisodeDto episode) {
        isEditingEpisode = true;
        layoutEpisodeDetails.setVisibility(View.VISIBLE);
        currentEditingEpisodeId = episode.getId();
        etVideoUrl.setText(episode.getVideoUrl() != null ? episode.getVideoUrl() : "");
        swPremium.setChecked(episode.isPremium());
        swDeleted.setChecked(episode.isDeleted());
        
        List<AdminMovieSaveRequest.AdminSubtitleRequest> existingSubs = new ArrayList<>();
        if (episode.getSubtitles() != null) {
            for (SubtitleDto sub : episode.getSubtitles()) {
                existingSubs.add(new AdminMovieSaveRequest.AdminSubtitleRequest(sub.getLanguage(), sub.getFileUrl()));
            }
        }
        subtitleAdapter.setSubtitles(existingSubs);
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

    private void saveCurrentEpisodeDataToLocal() {
        if (!isEditingEpisode) return;
        int position = episodeAdapter.getSelectedPosition();
        if (position >= 0 && position < episodeList.size()) {
            EpisodeDto ep = episodeList.get(position);
            ep.setVideoUrl(etVideoUrl.getText().toString().trim());
            ep.setPremium(swPremium.isChecked());
            ep.setDeleted(swDeleted.isChecked());
            List<SubtitleDto> subs = new ArrayList<>();
            for (AdminMovieSaveRequest.AdminSubtitleRequest subReq : subtitleAdapter.getSubtitles()) {
                SubtitleDto sub = new SubtitleDto();
                sub.setLanguage(subReq.getLanguage());
                sub.setFileUrl(subReq.getFileUrl());
                subs.add(sub);
            }
            ep.setSubtitles(subs);
        }
    }

    private void adjustEpisodeSelectionAfterDeletion() {
        if (episodeAdapter.getSelectedPosition() >= episodeList.size()) {
            int newSelected = episodeList.size() > 0 ? episodeList.size() - 1 : -1;
            episodeAdapter.setSelectedPosition(newSelected);
            if (newSelected >= 0) {
                loadEpisodeData(episodeList.get(newSelected));
            } else {
                layoutEpisodeDetails.setVisibility(View.GONE);
                isEditingEpisode = false;
                currentEditingEpisodeId = null;
            }
        }
    }

    private void saveAllEpisodes(int currentIndex) {
        if (currentIndex >= episodeList.size()) {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            Toast.makeText(AdminMovieDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EpisodeDto ep = episodeList.get(currentIndex);
        AdminEpisodeSaveRequest epReq = new AdminEpisodeSaveRequest();
        epReq.setTitle("Tập " + (currentIndex + 1));
        epReq.setOverview(ep.getOverview() != null ? ep.getOverview() : "");
        epReq.setVideoUrl(ep.getVideoUrl() != null ? ep.getVideoUrl() : "");
        epReq.setPremium(ep.isPremium());
        epReq.setDeleted(ep.isDeleted());
        
        List<AdminMovieSaveRequest.AdminSubtitleRequest> subReqs = new ArrayList<>();
        if (ep.getSubtitles() != null) {
            for (SubtitleDto s : ep.getSubtitles()) {
                subReqs.add(new AdminMovieSaveRequest.AdminSubtitleRequest(s.getLanguage(), s.getFileUrl()));
            }
        }
        epReq.setSubtitles(subReqs);

        Call<MediaDetailResponse> call = (ep.getId() == null)
                ? apiService.createEpisodeAdmin(movieId, epReq)
                : apiService.updateEpisodeAdmin(movieId, ep.getId(), epReq);

        call.enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                if (response.isSuccessful()) {
                    saveAllEpisodes(currentIndex + 1);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(AdminMovieDetailActivity.this, "Lỗi cập nhật tập " + (currentIndex + 1), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AdminMovieDetailActivity.this, "Lỗi kết nối khi lưu tập " + (currentIndex + 1), Toast.LENGTH_SHORT).show();
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

        int expectedEpisodes = 1;
        try { expectedEpisodes = Integer.parseInt(etExpectedEpisodes.getText().toString().trim()); } catch (Exception e) {}

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
        request.setExpectedEpisodes(expectedEpisodes);
        request.setMediaType(rbTvSeries.isChecked() ? "TV_SERIES" : "MOVIE");
        
        if (layoutEpisodes.getVisibility() == View.VISIBLE) {
            request.setVideoUrl(null);
            request.setPremium(false);
            request.setDeleted(false);
            request.setSubtitles(new ArrayList<>());
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (isCreateMode) {
            apiService.createMovie(request).enqueue(new Callback<MediaDetailResponse>() {
                @Override
                public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AdminMovieDetailActivity.this, "Tạo phim thành công!", Toast.LENGTH_SHORT).show();
                        previewData = null; // Clear cache
                        
                        if ("TV_SERIES".equals(response.body().getMediaType())) {
                            Intent intent = new Intent(AdminMovieDetailActivity.this, AdminMovieDetailActivity.class);
                            intent.putExtra("IS_CREATE_MODE", false);
                            intent.putExtra("MOVIE_ID", response.body().getId());
                            startActivity(intent);
                        }
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
                    if (response.isSuccessful()) {
                        if (layoutEpisodes.getVisibility() == View.VISIBLE) {
                            saveCurrentEpisodeDataToLocal();
                            saveAllEpisodes(0);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(AdminMovieDetailActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
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
