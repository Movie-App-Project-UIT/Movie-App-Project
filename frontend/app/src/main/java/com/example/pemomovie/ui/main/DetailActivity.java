package com.example.pemomovie.ui.main;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.CommentFragment;
import com.example.pemomovie.R;
import com.example.pemomovie.RateFragment;
import com.example.pemomovie.adapter.PosterAdapter;

public class DetailActivity extends AppCompatActivity {

    private  RecyclerView rvContent;

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




        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        ScrollView svInfo = findViewById(R.id.svInfo);
        FrameLayout detailContainer = findViewById(R.id.detailFragmentContainer);

        // màn hình bình luận
        android.widget.ImageView btnComment = findViewById(R.id.btnComment);
        btnComment.setOnClickListener(v -> {

            //ẩn phần dưới poster để hiển thị fragment
            svInfo.setVisibility(View.GONE);
            detailContainer.setVisibility(View.VISIBLE);

            Long currentMovieId = getIntent().getLongExtra("MOVIE_ID", -1);
            CommentFragment commentFragment = new CommentFragment();
            Bundle bundle = new Bundle();
            bundle.putLong("MEDIA_ID", currentMovieId);
            commentFragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_up,
                            0
                    )
                    .replace(
                            R.id.detailFragmentContainer,
                            commentFragment
                    )
                    .addToBackStack(null)
                    .commit();
        });


        rvContent = findViewById(R.id.rvContent);
        // Mặc định mở màn hình là tab gợi ý tương tự
        showSuggestionList();
        
        TextView tabSuggestion = findViewById(R.id.tabSuggestion);
        TextView tabEpisode = findViewById(R.id.tabEpisode);
        
        // Đặt vị trí ban đầu cho thanh gạch dưới sau khi giao diện đã được vẽ xong
        tabSuggestion.post(() -> animateIndicatorTo(tabSuggestion));
        tabSuggestion.setOnClickListener(v -> {
            animateIndicatorTo(tabSuggestion);
            showSuggestionList();
        });
        // Nếu không là phim bộ thì ẩn tabEpisode
        tabEpisode.setOnClickListener(v -> {
            animateIndicatorTo(tabEpisode);
            showEpisodeList();
        });


        com.example.pemomovie.utils.NavigationHelper.setupBottomNavigation(this);
        
        Long movieId = getIntent().getLongExtra("MOVIE_ID", -1);
        if (movieId != -1) {
            loadMovieDetails(movieId);
        }
    }

    private void animateIndicatorTo(View target) {
        View tabIndicator = findViewById(R.id.tabIndicator);
        // Tính toán vị trí để thanh indicator nằm ngay giữa chữ
        float targetX = target.getX() + (target.getWidth() - tabIndicator.getWidth()) / 2f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(tabIndicator, "translationX", targetX);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private java.util.List<com.example.pemomovie.dto.MediaItemDto> similarMovies = new java.util.ArrayList<>();
    private PosterAdapter similarAdapter;
    private com.example.pemomovie.adapter.EpisodeAdapter episodeAdapter;
    private boolean isMovie = true;

    // list poster
    private void showSuggestionList(){
        rvContent.setLayoutManager(new GridLayoutManager(this, 3));
        if (similarAdapter == null) {
            similarAdapter = new PosterAdapter(this, similarMovies);
        }
        rvContent.setAdapter(similarAdapter);
    }

    private void loadSimilarMovies(String genreName) {
        com.example.pemomovie.api.ApiClient.getApiService().getGenres().enqueue(new retrofit2.Callback<java.util.List<com.example.pemomovie.dto.GenreDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.pemomovie.dto.GenreDto>> call, retrofit2.Response<java.util.List<com.example.pemomovie.dto.GenreDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long genreId = null;
                    for (com.example.pemomovie.dto.GenreDto genre : response.body()) {
                        if (genre.getName().equalsIgnoreCase(genreName)) {
                            genreId = genre.getId();
                            break;
                        }
                    }
                    if (genreId != null) {
                        fetchMoviesByGenre(genreId);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.pemomovie.dto.GenreDto>> call, Throwable t) {}
        });
    }

    private void fetchMoviesByGenre(Long genreId) {
        com.example.pemomovie.api.ApiClient.getApiService().filterMedia(null, genreId, null, null, null, null, null, 0, 10).enqueue(new retrofit2.Callback<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> call, retrofit2.Response<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    similarMovies.clear();
                    similarMovies.addAll(response.body().getContent());
                    if (rvContent.getAdapter() == similarAdapter) {
                        similarAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> call, Throwable t) {}
        });
    }

    // list episode
    private void showEpisodeList(){
        rvContent.setLayoutManager(new GridLayoutManager(this, 3));
        if (episodeAdapter == null) {
            java.util.List<String> episodes = new java.util.ArrayList<>();
            if (isMovie) {
                episodes.add("Tập 1");
            } else {
                // If it's a series, mock some episodes for now
                for (int i = 1; i <= 10; i++) {
                    episodes.add("Tập " + i);
                }
            }
            episodeAdapter = new com.example.pemomovie.adapter.EpisodeAdapter(this, episodes);
        }
        rvContent.setAdapter(episodeAdapter);
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
        TextView txtVIP = findViewById(R.id.txt_VIP);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvStartRanking = findViewById(R.id.tvStartRanking);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvMediaType = findViewById(R.id.tvMediaType);
        TextView tvCountry = findViewById(R.id.tvCountry);
        TextView tvGenre = findViewById(R.id.tvGenre);
        TextView tvDesc = findViewById(R.id.tvDesc);
        TextView tvDirector = findViewById(R.id.tvDirector);
        TextView tvCast = findViewById(R.id.tvCast);

        tvTitle.setText(detail.getTitle());
        tvStartRanking.setText(String.format(java.util.Locale.US, "%.1f/10", detail.getVoteAverage()));
        tvDuration.setText(detail.getDuration() != null ? detail.getDuration() + " phút" : "N/A");
        
        if (detail.getDirectors() != null && !detail.getDirectors().isEmpty()) {
            java.util.List<String> dNames = new java.util.ArrayList<>();
            for (com.example.pemomovie.dto.CreditDto c : detail.getDirectors()) dNames.add(c.getName());
            tvDirector.setText(String.join(", ", dNames));
        } else {
            tvDirector.setText("N/A");
        }

        if (detail.getCast() != null && !detail.getCast().isEmpty()) {
            java.util.List<String> cNames = new java.util.ArrayList<>();
            for (com.example.pemomovie.dto.CreditDto c : detail.getCast()) cNames.add(c.getName());
            tvCast.setText(String.join(", ", cNames));
        } else {
            tvCast.setText("N/A");
        }

        String genresStr = detail.getGenres() != null ? String.join(", ", detail.getGenres()).replace("Phim ", "").replace("phim ", "") : "N/A";
        tvMediaType.setText(genresStr);
        tvCountry.setText(detail.getCountryName() != null ? detail.getCountryName() : "N/A");
        tvGenre.setText(detail.getLanguage());
        tvDesc.setText(detail.getOverview());

        //thêm nếu là phim lẻ thì không hiển thị tabEpisode
        isMovie = "MOVIE".equalsIgnoreCase(detail.getMediaType());
        TextView tabEpisode = findViewById(R.id.tabEpisode);
        if (isMovie) {
            tabEpisode.setVisibility(android.view.View.GONE);
        } else {
            tabEpisode.setVisibility(android.view.View.VISIBLE);
        }
        TextView tabSuggestion = findViewById(R.id.tabSuggestion);
        tabSuggestion.post(() -> animateIndicatorTo(tabSuggestion));
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

        // Xử lý nút Yêu thích (Tym)
        android.widget.ImageView btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            // Chuyển MediaDetailResponse sang MediaItemDto để lưu
            com.example.pemomovie.dto.MediaItemDto currentMovie = new com.example.pemomovie.dto.MediaItemDto();
            currentMovie.setId(detail.getId());
            currentMovie.setTitle(detail.getTitle());
            currentMovie.setPosterUrl(detail.getPosterUrl());
            currentMovie.setBackdropUrl(detail.getBackdropUrl());

            // Set màu ban đầu
            if (com.example.pemomovie.utils.FavoriteManager.isFavorite(this, detail.getId())) {
                btnFavorite.setImageResource(R.drawable.ic_heart);
                btnFavorite.setColorFilter(Color.parseColor("#FF1493")); // Đỏ hồng
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorites);
                btnFavorite.setColorFilter(null);
            }

            // Click sự kiện
            btnFavorite.setOnClickListener(v -> {
                boolean isAdded = com.example.pemomovie.utils.FavoriteManager.toggleFavorite(this, currentMovie);
                if (isAdded) {
                    btnFavorite.setImageResource(R.drawable.ic_heart);
                    btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
                    android.widget.Toast.makeText(this, "Đã thêm vào yêu thích", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    btnFavorite.setImageResource(R.drawable.ic_favorites);
                    btnFavorite.setColorFilter(null);
                    android.widget.Toast.makeText(this, "Đã bỏ yêu thích", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        ImageButton btnPlay = findViewById(R.id.btnPlayDetail);
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> {
                Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
                intent.putExtra("MOVIE_ID", detail.getId());
                startActivity(intent);
            });
        }
        
        // Cập nhật tab gợi ý tương tự dựa trên genre đầu tiên
        if (detail.getGenres() != null && !detail.getGenres().isEmpty()) {
            loadSimilarMovies(detail.getGenres().get(0));
        }
    }
}