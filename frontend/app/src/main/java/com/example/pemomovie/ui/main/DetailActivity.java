package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.CastAdapter;
import com.example.pemomovie.adapter.CommentAdapter;
import com.example.pemomovie.adapter.EpisodeAdapter;
import com.example.pemomovie.adapter.GenreTagAdapter;
import com.example.pemomovie.adapter.PosterAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.GenreDto;
import com.example.pemomovie.dto.MediaDetailResponse;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.PageResponseDto;
import com.example.pemomovie.dto.ReviewRequestDto;
import com.example.pemomovie.dto.ReviewResponseDto;
import com.example.pemomovie.utils.FavoriteManager;
import com.example.pemomovie.utils.GlobalHeaderHelper;
import com.example.pemomovie.utils.NavigationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private Long movieId = -1L;
    private ApiService apiService;
    private boolean isMovie = true;

    // Adapters
    private CastAdapter directorAdapter;
    private CastAdapter castAdapter;
    private GenreTagAdapter genreTagAdapter;
    private EpisodeAdapter episodeAdapter;
    private PosterAdapter similarAdapter;
    private CommentAdapter commentAdapter;

    // Lists
    private List<MediaItemDto> similarMovies = new ArrayList<>();
    private List<ReviewResponseDto> reviewList = new ArrayList<>();
    private Long replyingToReviewId = null;

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

        apiService = ApiClient.getApiService();

        NavigationHelper.setupBottomNavigation(this);
        new GlobalHeaderHelper(this).setupGlobalHeader(findViewById(R.id.globalHeaderInclude));

        movieId = getIntent().getLongExtra("MOVIE_ID", -1L);
        if (movieId != -1L) {
            loadMovieDetails(movieId);
            loadComments();
        } else {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMovieDetails(Long id) {
        apiService.getMediaDetail(id).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindDataToUi(response.body());
                } else {
                    Toast.makeText(DetailActivity.this, "Lỗi tải thông tin phim", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                Toast.makeText(DetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDataToUi(MediaDetailResponse detail) {
        ImageView ivBackdrop = findViewById(R.id.ivBackdrop);
        TextView txtVIP = findViewById(R.id.txt_VIP);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvStartRanking = findViewById(R.id.tvStartRanking);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvDesc = findViewById(R.id.tvDesc);
        TextView tvCountry = findViewById(R.id.tvCountry);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvLanguage = findViewById(R.id.tvLanguage);

        tvTitle.setText(detail.getTitle());
        tvStartRanking.setText(String.format(java.util.Locale.US, "%.1f", detail.getVoteAverage()));
        tvDuration.setText(detail.getDuration() != null ? detail.getDuration() + " phút" : "N/A");
        tvDesc.setText(detail.getOverview());
        tvCountry.setText(detail.getCountryName() != null ? detail.getCountryName() : "N/A");
        tvYear.setText(detail.getReleaseYear() != null ? String.valueOf(detail.getReleaseYear()) : "N/A");
        tvLanguage.setText(detail.getLanguage() != null ? detail.getLanguage() : "N/A");

        if (detail.isPremium()) {
            txtVIP.setVisibility(View.VISIBLE);
        } else {
            txtVIP.setVisibility(View.GONE);
        }

        if (detail.getBackdropUrl() != null) {
            Glide.with(this).load(detail.getBackdropUrl()).into(ivBackdrop);
        } else if (detail.getPosterUrl() != null) {
            Glide.with(this).load(detail.getPosterUrl()).into(ivBackdrop);
        }

        // Setup Buttons
        LinearLayout btnPlayDetailNew = findViewById(R.id.btnPlayDetailNew);
        btnPlayDetailNew.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
            intent.putExtra("MOVIE_ID", detail.getId());
            startActivity(intent);
        });

        LinearLayout btnLikeDetail = findViewById(R.id.btnLikeDetail);
        ImageView ivLikeIcon = findViewById(R.id.ivLikeIcon);
        
        MediaItemDto currentMovie = new MediaItemDto();
        currentMovie.setId(detail.getId());
        currentMovie.setTitle(detail.getTitle());
        currentMovie.setPosterUrl(detail.getPosterUrl());
        currentMovie.setBackdropUrl(detail.getBackdropUrl());

        if (FavoriteManager.isFavorite(this, detail.getId())) {
            ivLikeIcon.setImageResource(R.drawable.ic_heart);
            ivLikeIcon.setColorFilter(Color.parseColor("#FF1493"));
        } else {
            ivLikeIcon.setImageResource(R.drawable.ic_favorites);
            ivLikeIcon.setColorFilter(Color.WHITE);
        }

        btnLikeDetail.setOnClickListener(v -> {
            boolean isAdded = FavoriteManager.toggleFavorite(this, currentMovie);
            if (isAdded) {
                ivLikeIcon.setImageResource(R.drawable.ic_heart);
                ivLikeIcon.setColorFilter(Color.parseColor("#FF1493"));
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                ivLikeIcon.setImageResource(R.drawable.ic_favorites);
                ivLikeIcon.setColorFilter(Color.WHITE);
                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Directors
        RecyclerView rvDirectors = findViewById(R.id.rvDirectors);
        if (detail.getDirectors() != null && !detail.getDirectors().isEmpty()) {
            directorAdapter = new CastAdapter(this, detail.getDirectors());
            rvDirectors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvDirectors.setAdapter(directorAdapter);
            rvDirectors.setVisibility(View.VISIBLE);
        } else {
            rvDirectors.setVisibility(View.GONE);
        }

        // Setup Cast
        RecyclerView rvCast = findViewById(R.id.rvCast);
        if (detail.getCast() != null && !detail.getCast().isEmpty()) {
            castAdapter = new CastAdapter(this, detail.getCast());
            rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvCast.setAdapter(castAdapter);
            rvCast.setVisibility(View.VISIBLE);
        } else {
            rvCast.setVisibility(View.GONE);
        }

        // Setup Genres
        RecyclerView rvGenreTags = findViewById(R.id.rvGenreTags);
        if (detail.getGenres() != null && !detail.getGenres().isEmpty()) {
            genreTagAdapter = new GenreTagAdapter(this, detail.getGenres());
            rvGenreTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvGenreTags.setAdapter(genreTagAdapter);
            rvGenreTags.setVisibility(View.VISIBLE);
            // Load similar movies based on first genre
            loadSimilarMovies(detail.getGenres().get(0));
        } else {
            rvGenreTags.setVisibility(View.GONE);
        }

        // Setup Episodes
        isMovie = "MOVIE".equalsIgnoreCase(detail.getMediaType());
        LinearLayout layoutEpisodesContainer = findViewById(R.id.layoutEpisodesContainer);
        
        // --- BẮT ĐẦU CODE TEST (LUÔN HIỂN THỊ 12 TẬP) ---
        // if (isMovie) {
        //     layoutEpisodesContainer.setVisibility(View.GONE);
        // } else {
            layoutEpisodesContainer.setVisibility(View.VISIBLE);
            RecyclerView rvEpisodes = findViewById(R.id.rvEpisodes);
            List<String> episodes = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                episodes.add("Tập " + i);
            }
            episodeAdapter = new EpisodeAdapter(this, episodes);
            rvEpisodes.setLayoutManager(new GridLayoutManager(this, 3));
            rvEpisodes.setAdapter(episodeAdapter);
        // }
        // --- KẾT THÚC CODE TEST ---

        // Setup Comments UI elements
        RecyclerView rvComments = findViewById(R.id.rvComments);
        EditText edtComment = findViewById(R.id.edtComment);
        ImageButton btnSendComment = findViewById(R.id.btnSendComment);
        ImageView ivCurrentUserAvatar = findViewById(R.id.ivCurrentUserAvatar);
        
        if (ivCurrentUserAvatar != null) {
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                com.bumptech.glide.Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(ivCurrentUserAvatar);
            }
        }

        commentAdapter = new CommentAdapter(this, reviewList, new CommentAdapter.OnCommentActionClickListener() {
            @Override
            public void onReplyClick(ReviewResponseDto comment) {
                replyingToReviewId = comment.getId();
                edtComment.setHint("Trả lời @" + (comment.getUser() != null ? comment.getUser().getUsername() : "User") + "...");
                edtComment.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(edtComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onReportClick(ReviewResponseDto comment) {
                showReportDialog(comment.getId());
            }
        });
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnSendComment.setOnClickListener(v -> {
            String content = edtComment.getText().toString().trim();
            if (!content.isEmpty() && movieId != -1L) {
                postComment(content, edtComment);
            }
        });

        // Setup Similar Movies
        RecyclerView rvSuggestion = findViewById(R.id.rvSuggestion);
        similarAdapter = new PosterAdapter(this, similarMovies);
        rvSuggestion.setLayoutManager(new GridLayoutManager(this, 3));
        rvSuggestion.setAdapter(similarAdapter);
    }

    private void loadSimilarMovies(String genreName) {
        apiService.getGenres().enqueue(new Callback<List<GenreDto>>() {
            @Override
            public void onResponse(Call<List<GenreDto>> call, Response<List<GenreDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long genreId = null;
                    for (GenreDto genre : response.body()) {
                        if (genre.getName().equalsIgnoreCase(genreName)) {
                            genreId = genre.getId();
                            break;
                        }
                    }
                    if (genreId != null) {
                        apiService.filterMedia(null, genreId, null, null, null, null, null, 0, 10).enqueue(new Callback<PageResponseDto<MediaItemDto>>() {
                            @Override
                            public void onResponse(Call<PageResponseDto<MediaItemDto>> call, Response<PageResponseDto<MediaItemDto>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    similarMovies.clear();
                                    for(MediaItemDto item : response.body().getContent()) {
                                        if(!item.getId().equals(movieId)) {
                                            similarMovies.add(item);
                                        }
                                    }
                                    if (similarAdapter != null) similarAdapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onFailure(Call<PageResponseDto<MediaItemDto>> call, Throwable t) {}
                        });
                    }
                }
            }
            @Override
            public void onFailure(Call<List<GenreDto>> call, Throwable t) {}
        });
    }

    private void loadComments() {
        if (movieId == null || movieId == -1L || apiService == null) return;
        apiService.getReviews(movieId).enqueue(new Callback<List<ReviewResponseDto>>() {
            @Override
            public void onResponse(Call<List<ReviewResponseDto>> call, Response<List<ReviewResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reviewList.clear();
                    // Limiting to 20 for initial load
                    List<ReviewResponseDto> allRoots = response.body();
                    int count = 0;
                    for (ReviewResponseDto root : allRoots) {
                        if (count >= 20) break;
                        reviewList.add(root);
                        flattenReplies(reviewList, root.getReplies());
                        count++;
                    }
                    if (commentAdapter != null) commentAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<ReviewResponseDto>> call, Throwable t) {}
        });
    }

    private void flattenReplies(List<ReviewResponseDto> targetList, List<ReviewResponseDto> replies) {
        if (replies == null) return;
        for (ReviewResponseDto r : replies) {
            targetList.add(r);
            flattenReplies(targetList, r.getReplies());
        }
    }

    private void postComment(String content, EditText etComment) {
        ReviewRequestDto req = new ReviewRequestDto(movieId, null, replyingToReviewId, content);
        apiService.postReview(req).enqueue(new Callback<ReviewResponseDto>() {
            @Override
            public void onResponse(Call<ReviewResponseDto> call, Response<ReviewResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etComment.setText("");
                    etComment.setHint("Thêm bình luận...");
                    replyingToReviewId = null;
                    loadComments();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                } else {
                    Toast.makeText(DetailActivity.this, "Lỗi khi đăng bình luận", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ReviewResponseDto> call, Throwable t) {}
        });
    }

    private void showReportDialog(Long reviewId) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_report_comment);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        EditText etReportReason = dialog.findViewById(R.id.etReportReason);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnCancelReport);
        android.widget.Button btnSubmit = dialog.findViewById(R.id.btnSubmitReport);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String reason = etReportReason.getText().toString().trim();
            Map<String, String> payload = new java.util.HashMap<>();
            payload.put("reason", reason);
            apiService.reportReview(reviewId, payload).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(DetailActivity.this, "Báo cáo thành công", Toast.LENGTH_SHORT).show();
                        Set<Long> idsToRemove = new HashSet<>();
                        idsToRemove.add(reviewId);
                        boolean added;
                        do {
                            added = false;
                            for (ReviewResponseDto r : reviewList) {
                                if (!idsToRemove.contains(r.getId()) && r.getParentId() != null && idsToRemove.contains(r.getParentId())) {
                                    idsToRemove.add(r.getId());
                                    added = true;
                                }
                            }
                        } while (added);

                        Iterator<ReviewResponseDto> iterator = reviewList.iterator();
                        while (iterator.hasNext()) {
                            if (idsToRemove.contains(iterator.next().getId())) {
                                iterator.remove();
                            }
                        }
                        if (commentAdapter != null) commentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(DetailActivity.this, "Bạn đã báo cáo hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(DetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }
}