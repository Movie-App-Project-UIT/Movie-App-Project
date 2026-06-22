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

    @Override
    protected void onResume() {
        super.onResume();
        if (movieId != -1L) {
            // Reload comments when returning to DetailActivity to show new comments.
            // Reset to page 0.
            loadComments();
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
            apiService.getHistoryByMedia(detail.getId()).enqueue(new retrofit2.Callback<com.example.pemomovie.dto.WatchHistoryItemDto>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.pemomovie.dto.WatchHistoryItemDto> call, retrofit2.Response<com.example.pemomovie.dto.WatchHistoryItemDto> response) {
                    Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                    intent.putExtra("MOVIE_ID", detail.getId());
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.pemomovie.dto.WatchHistoryItemDto history = response.body();
                        if (history.getEpisode() != null) {
                            intent.putExtra("EPISODE_ID", history.getEpisode().getId());
                        } else {
                            intent.putExtra("EPISODE_ID", -1L);
                        }
                        if (history.getProgressSeconds() != null) {
                            intent.putExtra("START_POSITION", history.getProgressSeconds()); // Pass in seconds
                        }
                    } else {
                        intent.putExtra("EPISODE_ID", -1L);
                    }
                    startActivity(intent);
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.pemomovie.dto.WatchHistoryItemDto> call, Throwable t) {
                    Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                    intent.putExtra("MOVIE_ID", detail.getId());
                    intent.putExtra("EPISODE_ID", -1L);
                    startActivity(intent);
                }
            });
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
        
        // if (isMovie) {
        //     layoutEpisodesContainer.setVisibility(View.GONE);
        // } else {
            layoutEpisodesContainer.setVisibility(View.VISIBLE);
            RecyclerView rvEpisodes = findViewById(R.id.rvEpisodes);
            
            java.util.List<com.example.pemomovie.dto.EpisodeDto> episodes = new java.util.ArrayList<>();
            if (detail.getSeasons() != null && !detail.getSeasons().isEmpty()) {
                com.example.pemomovie.dto.SeasonDto firstSeason = detail.getSeasons().get(0);
                if (firstSeason.getEpisodes() != null) {
                    for (com.example.pemomovie.dto.EpisodeDto ep : firstSeason.getEpisodes()) {
                        boolean hasData = (ep.getVideoUrl() != null && !ep.getVideoUrl().trim().isEmpty()) || 
                                          (ep.getSubtitles() != null && !ep.getSubtitles().isEmpty());
                        if (!ep.isDeleted() && hasData) {
                            episodes.add(ep);
                        }
                    }
                }
            }
            
            episodeAdapter = new EpisodeAdapter(this, episodes, (episode, position) -> {
                Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                intent.putExtra("MOVIE_ID", detail.getId());
                intent.putExtra("EPISODE_ID", episode.getId());
                startActivity(intent);
            });
            rvEpisodes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
            rvEpisodes.setAdapter(episodeAdapter);
        // }

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
                ReplyBottomSheetFragment bottomSheet = new ReplyBottomSheetFragment(comment, movieId);
                bottomSheet.show(getSupportFragmentManager(), "ReplyBottomSheet");
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

        // Kiểm tra lịch sử xem phim
        com.example.pemomovie.api.ApiClient.getApiService().getUserHistory().enqueue(new retrofit2.Callback<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> call, retrofit2.Response<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (com.example.pemomovie.dto.WatchHistoryItemDto history : response.body()) {
                        Long mId = null;
                        int totalDuration = 0;
                        if (history.getMedia() != null) {
                            mId = history.getMedia().getId();
                            if (history.getEpisode() != null) {
                                if (history.getEpisode().getDuration() != null) totalDuration = history.getEpisode().getDuration();
                            } else {
                                if (history.getMedia().getDuration() != null) totalDuration = history.getMedia().getDuration();
                            }
                        }

                        if (mId != null && mId.equals(detail.getId())) {
                            int progress = history.getProgressSeconds() != null ? history.getProgressSeconds() : 0;
                            int totalDurationSec = 0;
                            if (history.getTotalDurationSeconds() != null && history.getTotalDurationSeconds() > 0) {
                                totalDurationSec = history.getTotalDurationSeconds();
                            } else if (totalDuration > 0) {
                                totalDurationSec = totalDuration * 60;
                            }
                            float percent = totalDurationSec > 0 ? (float) progress / totalDurationSec : 0;
                            
                            // Nếu chưa xem xong hẳn (< 95%)
                            if (percent < 0.95f && progress > 0) {
                                LinearLayout btnPlayDetailNew = findViewById(R.id.btnPlayDetailNew);
                                if (btnPlayDetailNew != null) {
                                    btnPlayDetailNew.setOnClickListener(v -> {
                                        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(DetailActivity.this)
                                                .setTitle("Tiếp tục xem phim")
                                                .setMessage("Bạn đang xem dở bộ phim này. Bạn muốn xem tiếp hay bắt đầu lại từ đầu?")
                                                .setPositiveButton("Xem tiếp", (d, which) -> {
                                                    Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                                                    intent.putExtra("MOVIE_ID", detail.getId());
                                                    if (history.getEpisode() != null) intent.putExtra("EPISODE_ID", history.getEpisode().getId());
                                                    intent.putExtra("START_POSITION", (long) progress);
                                                    startActivity(intent);
                                                })
                                                .setNegativeButton("Từ đầu", (d, which) -> {
                                                    Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                                                    intent.putExtra("MOVIE_ID", detail.getId());
                                                    if (history.getEpisode() != null) intent.putExtra("EPISODE_ID", history.getEpisode().getId());
                                                    intent.putExtra("START_POSITION", 0L);
                                                    startActivity(intent);
                                                })
                                                .create();
                                            dialog.show();
                                            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#F7328E"));
                                            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.parseColor("#F7328E"));
                                    });
                                }
                            }
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.pemomovie.dto.WatchHistoryItemDto>> call, Throwable t) {}
        });
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
                        apiService.filterMedia(null, java.util.Collections.singletonList(genreId), null, null, null, null, null, null, null, 0, 10).enqueue(new Callback<PageResponseDto<MediaItemDto>>() {
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

    private List<ReviewResponseDto> allRootComments = new ArrayList<>();
    private int currentCommentPage = 0;
    private final int COMMENTS_PER_PAGE = 5;

    private void loadComments() {
        if (movieId == null || movieId == -1L || apiService == null) return;
        apiService.getReviews(movieId).enqueue(new Callback<List<ReviewResponseDto>>() {
            @Override
            public void onResponse(Call<List<ReviewResponseDto>> call, Response<List<ReviewResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRootComments = response.body();
                    currentCommentPage = 0;
                    showCommentPage(0);
                }
            }
            @Override
            public void onFailure(Call<List<ReviewResponseDto>> call, Throwable t) {}
        });
    }

    private void showCommentPage(int page) {
        if (allRootComments == null || allRootComments.isEmpty()) {
            LinearLayout layoutPagination = findViewById(R.id.layoutPagination);
            if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);
            return;
        }

        currentCommentPage = page;
        int totalPages = (int) Math.ceil((double) allRootComments.size() / COMMENTS_PER_PAGE);

        int start = currentCommentPage * COMMENTS_PER_PAGE;
        int end = Math.min(start + COMMENTS_PER_PAGE, allRootComments.size());

        reviewList.clear();
        for (int i = start; i < end; i++) {
            ReviewResponseDto root = allRootComments.get(i);
            reviewList.add(root);
        }

        if (commentAdapter != null) {
            commentAdapter.notifyDataSetChanged();
        }

        TextView tvCommentTitle = findViewById(R.id.tvCommentTitle);
        if (tvCommentTitle != null) {
            int totalComments = 0;
            for (ReviewResponseDto r : allRootComments) {
                totalComments += 1 + countReplies(r.getReplies());
            }
            tvCommentTitle.setText("Bình luận (" + totalComments + ")");
        }

        setupPaginationUI(totalPages);
    }

    private int countReplies(List<ReviewResponseDto> replies) {
        if (replies == null || replies.isEmpty()) return 0;
        int count = replies.size();
        for (ReviewResponseDto r : replies) {
            count += countReplies(r.getReplies());
        }
        return count;
    }

    private com.example.pemomovie.adapter.PaginationAdapter paginationAdapter;

    private void setupPaginationUI(int totalPages) {
        LinearLayout layoutPagination = findViewById(R.id.layoutPagination);
        if (layoutPagination == null) return;

        if (totalPages <= 1) {
            layoutPagination.setVisibility(View.GONE);
            return;
        }

        layoutPagination.setVisibility(View.VISIBLE);

        androidx.recyclerview.widget.RecyclerView rvPagination = findViewById(R.id.rvPagination);
        ImageButton btnPrevPage = findViewById(R.id.btnPrevPage);
        ImageButton btnNextPage = findViewById(R.id.btnNextPage);

        if (paginationAdapter == null) {
            paginationAdapter = new com.example.pemomovie.adapter.PaginationAdapter(this, totalPages, page -> {
                showCommentPage(page);
            });
            rvPagination.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            rvPagination.setAdapter(paginationAdapter);
        } else {
            paginationAdapter.setTotalPages(totalPages);
        }
        
        paginationAdapter.setCurrentPage(currentCommentPage);
        rvPagination.scrollToPosition(currentCommentPage);

        btnPrevPage.setAlpha(currentCommentPage > 0 ? 1.0f : 0.3f);
        btnPrevPage.setEnabled(currentCommentPage > 0);
        btnPrevPage.setOnClickListener(v -> {
            if (currentCommentPage > 0) showCommentPage(currentCommentPage - 1);
        });

        btnNextPage.setAlpha(currentCommentPage < totalPages - 1 ? 1.0f : 0.3f);
        btnNextPage.setEnabled(currentCommentPage < totalPages - 1);
        btnNextPage.setOnClickListener(v -> {
            if (currentCommentPage < totalPages - 1) showCommentPage(currentCommentPage + 1);
        });
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
                        loadComments();
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