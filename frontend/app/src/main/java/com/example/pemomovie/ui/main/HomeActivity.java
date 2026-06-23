package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.SectionAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.model.Section;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.example.pemomovie.utils.NavigationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private androidx.viewpager2.widget.ViewPager2 bannerViewPager;
    private RecyclerView sectionListHome;
    private Handler handler;
    private Runnable sliderRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            
            View header = findViewById(R.id.globalHeaderInclude);
            if (header != null) {
                header.setPadding(0, systemBars.top, 0, 0);
            }
            
            View bottomNav = findViewById(R.id.bottomNavInclude);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        bannerViewPager = findViewById(R.id.bannerViewPager);
        sectionListHome = findViewById(R.id.sectionListHome);
        sectionListHome.setLayoutManager(new LinearLayoutManager(this));
        sectionListHome.setHasFixedSize(true);
        sectionListHome.setItemViewCacheSize(4);

        handler = new Handler(Looper.getMainLooper());

        NavigationHelper.setupBottomNavigation(this);
        // Khởi tạo Global Header mới
        new com.example.pemomovie.utils.GlobalHeaderHelper(this).setupGlobalHeader(findViewById(R.id.globalHeaderInclude));

        sliderRunnable = () -> {
            if (bannerViewPager != null && bannerViewPager.getAdapter() != null) {
                int currentItem = bannerViewPager.getCurrentItem();
                int totalItems = bannerViewPager.getAdapter().getItemCount();
                if (totalItems > 0) {
                    int nextItem = (currentItem + 1) % totalItems;
                    bannerViewPager.setCurrentItem(nextItem, true);
                }
            }
        };

        // Đồng bộ danh sách phim yêu thích với Backend khi vừa mở App
        com.example.pemomovie.utils.FavoriteManager.syncFavoritesWithBackend(this, null);

        fetchHomepageData();
    }

    private void fetchHomepageData() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getHomepageData().enqueue(new Callback<Map<String, List<MediaItemDto>>>() {
            @Override
            public void onResponse(Call<Map<String, List<MediaItemDto>>> call, Response<Map<String, List<MediaItemDto>>> response) {
                if (isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, List<MediaItemDto>> data = response.body();
                    List<Section> sections = new ArrayList<>();

                    List<MediaItemDto> trending = data.get("trending");
                    if (trending != null && !trending.isEmpty()) {
                        sections.add(new Section("Đang thịnh hành", trending));

                        // Set banner ViewPager
                        List<MediaItemDto> bannerMovies = trending.size() > 5 ? trending.subList(0, 5) : trending;
                        com.example.pemomovie.adapter.BannerAdapter bannerAdapter = new com.example.pemomovie.adapter.BannerAdapter(HomeActivity.this, bannerMovies);
                        bannerViewPager.setAdapter(bannerAdapter);
                        
                        int startPosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % bannerMovies.size());
                        bannerViewPager.setCurrentItem(startPosition, false);
                        
                        Button btnPlay = findViewById(R.id.btnPlay);
                        Button btnDetail = findViewById(R.id.btnDetail);

                        bannerViewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) {
                                super.onPageSelected(position);
                                handler.removeCallbacks(sliderRunnable);
                                handler.postDelayed(sliderRunnable, 5000);
                                
                                int realPosition = position % bannerMovies.size();
                                MediaItemDto currentMovie = bannerMovies.get(realPosition);
                                
                                if (btnDetail != null) {
                                    btnDetail.setOnClickListener(v -> {
                                        Intent intent = new Intent(HomeActivity.this, DetailActivity.class);
                                        intent.putExtra("MOVIE_ID", currentMovie.getId());
                                        startActivity(intent);
                                    });
                                }

                                if (btnPlay != null) {
                                    btnPlay.setOnClickListener(v -> {
                                        Intent intent = new Intent(HomeActivity.this, PlayActivity.class);
                                        intent.putExtra("MOVIE_ID", currentMovie.getId());
                                        startActivity(intent);
                                    });
                                }
                            }
                        });
                    }


                    List<MediaItemDto> topRated = data.get("topRated");
                    if (topRated != null && !topRated.isEmpty()) {
                        sections.add(new Section("Đánh giá cao", topRated));
                    }

                    SectionAdapter adapter = new SectionAdapter(HomeActivity.this, sections);
                    sectionListHome.setAdapter(adapter);
                } else {
                    Log.e("HomeActivity", "Failed to fetch homepage data");
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<MediaItemDto>>> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching homepage data", t);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && sliderRunnable != null) {
            handler.removeCallbacks(sliderRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handler != null && sliderRunnable != null) {
            handler.postDelayed(sliderRunnable, 5000);
        }
    }
}
