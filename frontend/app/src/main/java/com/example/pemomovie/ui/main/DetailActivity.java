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

        //tạo màu linear cho IMDb
        TextView txtIMDb = findViewById(R.id.txt_IMDb);
        Shader textShader = new LinearGradient(0, 0, 0, txtIMDb.getTextSize(),
                new int[]{Color.parseColor("#6C29D6"), Color.parseColor("#F43393")}, null, Shader.TileMode.CLAMP);

        txtIMDb.getPaint().setShader(textShader);


        // Xem video
        ImageButton btnPlay = findViewById(R.id.btnPlayDetail);
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, PlayActivity.class);
//            intent.putExtra("MOVIE_ID", bannerMovie.getId());
//            startActivity(intent);
        });

        ScrollView svInfo = findViewById(R.id.svInfo);
        FrameLayout detailContainer = findViewById(R.id.detailFragmentContainer);

        // màn hình bình luận
        ImageButton btnComment = findViewById(R.id.btnComment);
        btnComment.setOnClickListener(v -> {

            //ẩn phần dưới poster để hiển thị fragment
            svInfo.setVisibility(View.GONE);
            detailContainer.setVisibility(View.VISIBLE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_up,
                            0
                    )
                    .replace(
                            R.id.detailFragmentContainer,
                            new CommentFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        // màn hình đánh giá
        ImageButton btnRate = findViewById(R.id.btnRate);
        btnRate.setOnClickListener(v -> {

            //ẩn phần dưới poster để hiển thị fragment
            svInfo.setVisibility(View.GONE);
            detailContainer.setVisibility(View.VISIBLE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_up,
                            0
                    )
                    .replace(
                            R.id.detailFragmentContainer,
                            new RateFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });


        rvContent = findViewById(R.id.rvContent);
        // Mặc định mở màn hình là tab gợi ý tương tự
        showSuggestionList();

        //click tab gợi ý và tab tập phim
        TextView tabSuggestion = findViewById(R.id.tabSuggestion);
        TextView tabEpisode = findViewById(R.id.tabEpisode);
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
        float targetX = target.getX();
        ObjectAnimator animator = ObjectAnimator.ofFloat(tabIndicator, "translationX", targetX);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    // list poster
    private void showSuggestionList(){
        // hiển thị list poster theo 2 poster mỗi hàng
        rvContent.setLayoutManager(new GridLayoutManager(this, 2));

        //adapter....
    }

    // list episode
    private  void showEpisodeList(){
        // kiểm tra điều kiện nếu là phim bộ thì hiển thị tập phim

        // hiển thị list ập phim theo 3 tập mỗi hàng
        rvContent.setLayoutManager(new GridLayoutManager(this, 3));
        //adapter...

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

        //thêm nếu là phim lẻ thì không hiển thị tabEpisode

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