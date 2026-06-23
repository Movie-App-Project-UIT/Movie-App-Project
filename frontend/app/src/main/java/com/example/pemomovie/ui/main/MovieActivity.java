package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.FilterChipAdapter;
import com.example.pemomovie.adapter.PosterAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.dto.AgeRatingDto;
import com.example.pemomovie.dto.CountryDto;
import com.example.pemomovie.dto.FilterOption;
import com.example.pemomovie.dto.GenreDto;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.PageResponseDto;
import com.example.pemomovie.utils.NavigationHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieActivity extends AppCompatActivity {
    private com.example.pemomovie.utils.GlobalHeaderHelper globalHeaderHelper;

    private LinearLayout btnSortOptions;
    private TextView txtCurrentSort;

    private TabLayout tabLayoutMediaType;
    private LinearLayout btnToggleFilter, layoutFiltersContainer;
    private TextView txtToggleFilter;
    
    private RecyclerView rvCountries, rvGenres, rvYears, rvMovies;
    
    private com.example.pemomovie.adapter.MultiSelectFilterChipAdapter countryAdapter, genreAdapterMulti, languageAdapter;
    private FilterChipAdapter yearAdapter;
    private PosterAdapter movieAdapter;

    private ApiService apiService;
    
    // Filter State
    private String currentKeyword = null;
    private String currentMediaType = null; // null = Tất cả
    private java.util.List<Long> currentGenreIds = new java.util.ArrayList<>();
    private java.util.List<Long> currentCountryIds = new java.util.ArrayList<>();
    private java.util.List<String> currentLanguages = new java.util.ArrayList<>();
    private Long currentAgeRatingId = null;
    private Integer currentReleaseYear = null;
    private Boolean currentIsPremium = null;
    private String currentSortBy = "Mới nhất"; // "Mới nhất", "Cũ nhất", "Điểm TMDB"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NavigationHelper.setupBottomNavigation(this);

        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupToggleFilter();
        setupTabLayout();
        
        setupStaticFilters();
        loadDynamicFilters();
        
        // Initial load
        loadMovies();
    }

    private void initViews() {
        // Khởi tạo thanh tìm kiếm chung
        globalHeaderHelper = new com.example.pemomovie.utils.GlobalHeaderHelper(this);
        globalHeaderHelper.setupGlobalHeader(findViewById(R.id.globalHeaderInclude));

        tabLayoutMediaType = findViewById(R.id.tabLayoutMediaType);
        btnToggleFilter = findViewById(R.id.btnToggleFilter);
        layoutFiltersContainer = findViewById(R.id.layoutFiltersContainer);
        txtToggleFilter = findViewById(R.id.txtToggleFilter);

        rvCountries = findViewById(R.id.rvCountries);
        rvGenres = findViewById(R.id.rvGenres);
        rvYears = findViewById(R.id.rvYears);
        androidx.recyclerview.widget.RecyclerView rvLanguages = findViewById(R.id.rvLanguages);
        rvMovies = findViewById(R.id.rvMovies);
        androidx.recyclerview.widget.RecyclerView rvPremium = findViewById(R.id.rvPremium);
        
        btnSortOptions = findViewById(R.id.btnSortOptions);
        txtCurrentSort = findViewById(R.id.txtCurrentSort);

        rvMovies.setLayoutManager(new GridLayoutManager(this, 3));
        movieAdapter = new PosterAdapter(this, new ArrayList<>());
        rvMovies.setAdapter(movieAdapter);
    }

    private void setupToggleFilter() {
        btnToggleFilter.setOnClickListener(v -> {
            if (layoutFiltersContainer.getVisibility() == View.VISIBLE) {
                layoutFiltersContainer.setVisibility(View.GONE);
                txtToggleFilter.setText("Mở rộng bộ lọc");
            } else {
                layoutFiltersContainer.setVisibility(View.VISIBLE);
                txtToggleFilter.setText("Thu gọn bộ lọc");
            }
        });
    }

    private void setupTabLayout() {
        tabLayoutMediaType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentMediaType = null; break; // Tất cả
                    case 1: currentMediaType = "MOVIE"; break; // Phim Lẻ
                    case 2: currentMediaType = "TV_SERIES"; break; // Phim Bộ
                }
                loadMovies();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupStaticFilters() {
        // Years
        List<FilterOption> years = new ArrayList<>();
        years.add(new FilterOption((Integer)null, "Tất cả"));
        for (int i = 2026; i >= 2010; i--) {
            years.add(new FilterOption(i, String.valueOf(i)));
        }
        yearAdapter = new FilterChipAdapter(years, option -> {
            currentReleaseYear = option.getIntValue();
            loadMovies();
        });
        rvYears.setAdapter(yearAdapter);

        // Sort By
        if (btnSortOptions != null) {
            btnSortOptions.setOnClickListener(v -> {
                android.view.View popupView = getLayoutInflater().inflate(R.layout.layout_dropdown_sort, null);
                
                int width = (int) (160 * getResources().getDisplayMetrics().density);
                int height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                
                final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(popupView, width, height, focusable);
                popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    popupWindow.setElevation(10);
                }

                android.widget.TextView btnSortNewest = popupView.findViewById(R.id.btnSortNewest);
                android.widget.TextView btnSortOldest = popupView.findViewById(R.id.btnSortOldest);
                android.widget.TextView btnSortRating = popupView.findViewById(R.id.btnSortRating);

                android.view.View.OnClickListener listener = new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        String title = ((android.widget.TextView) view).getText().toString();
                        if (txtCurrentSort != null) {
                            txtCurrentSort.setText("Sắp xếp: " + title);
                        }
                        currentSortBy = title;
                        loadMovies();
                        popupWindow.dismiss();
                    }
                };

                btnSortNewest.setOnClickListener(listener);
                btnSortOldest.setOnClickListener(listener);
                btnSortRating.setOnClickListener(listener);

                // Show as dropdown aligned to the end
                int yoff = (int) (4 * getResources().getDisplayMetrics().density);
                popupWindow.showAsDropDown(btnSortOptions, 0, yoff, android.view.Gravity.END);
            });
        }
    }

    private void loadDynamicFilters() {
        // Genres
        apiService.getGenres().enqueue(new Callback<List<GenreDto>>() {
            @Override
            public void onResponse(Call<List<GenreDto>> call, Response<List<GenreDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FilterOption> options = new ArrayList<>();
                    options.add(new FilterOption((Long)null, "Tất cả"));
                    for (GenreDto g : response.body()) options.add(new FilterOption(g.getId(), g.getName()));
                    genreAdapterMulti = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(options, opts -> {
                        currentGenreIds.clear();
                        for (FilterOption opt : opts) {
                            if (opt.getId() != null) {
                                currentGenreIds.add(opt.getId());
                            }
                        }
                    });
                    rvGenres.setAdapter(genreAdapterMulti);
                    
                    String genreName = getIntent().getStringExtra("GENRE_NAME");
                    if (genreName != null && !genreName.isEmpty()) {
                        genreAdapterMulti.selectByName(genreName);
                        // We also trigger loadMovies because it's passed directly via intent
                        loadMovies();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<GenreDto>> call, Throwable t) {}
        });

        // Countries
        apiService.getCountries().enqueue(new Callback<List<CountryDto>>() {
            @Override
            public void onResponse(Call<List<CountryDto>> call, Response<List<CountryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FilterOption> options = new ArrayList<>();
                    options.add(new FilterOption((Long)null, "Tất cả"));
                    for (CountryDto c : response.body()) options.add(new FilterOption(c.getId(), c.getName()));
                    countryAdapter = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(options, opts -> {
                        currentCountryIds.clear();
                        for (FilterOption opt : opts) {
                            if (opt.getId() != null) {
                                currentCountryIds.add(opt.getId());
                            }
                        }
                    });
                    rvCountries.setAdapter(countryAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<CountryDto>> call, Throwable t) {}
        });

        // Age Ratings đã bị ẩn
        // Premium
        List<FilterOption> premiumOptions = new ArrayList<>();
        premiumOptions.add(new FilterOption(0L, "Tất cả"));
        premiumOptions.add(new FilterOption(1L, "Premium"));
        premiumOptions.add(new FilterOption(2L, "Miễn phí"));
        FilterChipAdapter premiumAdapter = new FilterChipAdapter(premiumOptions, opt -> {
            if (opt.getName().equals("Premium")) currentIsPremium = true;
            else if (opt.getName().equals("Miễn phí")) currentIsPremium = false;
            else currentIsPremium = null;
        });
        androidx.recyclerview.widget.RecyclerView rvPremium = findViewById(R.id.rvPremium);
        rvPremium.setAdapter(premiumAdapter);
        
        // Languages
        List<FilterOption> langOptions = new ArrayList<>();
        langOptions.add(new FilterOption((Long)null, "Tất cả"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Việt"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Anh"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Hàn"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Trung"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Nhật"));
        langOptions.add(new FilterOption((Long)null, "Tiếng Thái"));

        languageAdapter = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(langOptions, opts -> {
            currentLanguages.clear();
            for (FilterOption opt : opts) {
                if (opt.getName() != null && !opt.getName().equals("Tất cả")) {
                    currentLanguages.add(opt.getName());
                }
            }
        });
        androidx.recyclerview.widget.RecyclerView rvLanguages = findViewById(R.id.rvLanguages);
        rvLanguages.setAdapter(languageAdapter);
        
        // Setup Clear and Apply Buttons
        TextView btnClearFilters = findViewById(R.id.btnClearFilters);
        androidx.appcompat.widget.AppCompatButton btnApplyFilters = findViewById(R.id.btnApplyFilters);
        
        btnClearFilters.setOnClickListener(v -> {
            if (genreAdapterMulti != null) genreAdapterMulti.clearSelection();
            if (countryAdapter != null) countryAdapter.clearSelection();
            if (languageAdapter != null) languageAdapter.clearSelection();
            currentReleaseYear = null;
            currentIsPremium = null;
            // Also reset UI for static filters here if needed
            loadMovies();
        });
        
        btnApplyFilters.setOnClickListener(v -> {
            layoutFiltersContainer.setVisibility(View.GONE);
            txtToggleFilter.setText("Mở rộng bộ lọc");
            loadMovies();
        });
    }

    private void loadMovies() {
        apiService.filterMedia(
                currentKeyword, 
                currentGenreIds.isEmpty() ? null : currentGenreIds, 
                currentCountryIds.isEmpty() ? null : currentCountryIds, 
                currentLanguages.isEmpty() ? null : currentLanguages, 
                currentAgeRatingId,
                currentReleaseYear, currentMediaType, currentSortBy, currentIsPremium, 0, 50
        ).enqueue(new Callback<PageResponseDto<MediaItemDto>>() {
            @Override
            public void onResponse(Call<PageResponseDto<MediaItemDto>> call, Response<PageResponseDto<MediaItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieAdapter.updateData(response.body().getContent());
                }
            }

            @Override
            public void onFailure(Call<PageResponseDto<MediaItemDto>> call, Throwable t) {
                Toast.makeText(MovieActivity.this, "Lỗi tải phim", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (globalHeaderHelper != null) {
            globalHeaderHelper.fetchNotifications();
        }
    }
}
