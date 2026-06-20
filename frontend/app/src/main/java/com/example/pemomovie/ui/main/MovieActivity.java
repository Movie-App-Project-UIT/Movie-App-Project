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

    private EditText edtSearch;
    private Button btnSearch;
    private TabLayout tabLayoutMediaType;
    private LinearLayout btnToggleFilter, layoutFiltersContainer;
    private TextView txtToggleFilter;
    
    private RecyclerView rvCountries, rvGenres, rvAgeRatings, rvYears, rvSorts, rvMovies;
    
    private FilterChipAdapter countryAdapter, genreAdapter, ageAdapter, yearAdapter, sortAdapter;
    private PosterAdapter movieAdapter;

    private ApiService apiService;
    
    // Filter State
    private String currentKeyword = null;
    private String currentMediaType = null; // null = Tất cả
    private Long currentGenreId = null;
    private Long currentCountryId = null;
    private Long currentAgeRatingId = null;
    private Integer currentReleaseYear = null;
    private String currentSortBy = "createdAt_desc"; // Mới nhất

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
        setupSearch();
        
        setupStaticFilters();
        loadDynamicFilters();
        
        // Initial load
        loadMovies();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        GradientTextView.applyHorizontalGradient(btnSearch, Color.parseColor("#6C29D6"), Color.parseColor("#F43393"));

        tabLayoutMediaType = findViewById(R.id.tabLayoutMediaType);
        btnToggleFilter = findViewById(R.id.btnToggleFilter);
        layoutFiltersContainer = findViewById(R.id.layoutFiltersContainer);
        txtToggleFilter = findViewById(R.id.txtToggleFilter);

        rvCountries = findViewById(R.id.rvCountries);
        rvGenres = findViewById(R.id.rvGenres);
        rvAgeRatings = findViewById(R.id.rvAgeRatings);
        rvYears = findViewById(R.id.rvYears);
        rvSorts = findViewById(R.id.rvSorts);
        rvMovies = findViewById(R.id.rvMovies);

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

    private void setupSearch() {
        btnSearch.setOnClickListener(v -> {
            currentKeyword = edtSearch.getText().toString().trim();
            if (currentKeyword.isEmpty()) currentKeyword = null;
            loadMovies();
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
        List<FilterOption> sorts = new ArrayList<>();
        sorts.add(new FilterOption(1L, "Mới nhất"));
        sorts.add(new FilterOption(2L, "Cũ nhất"));
        sorts.add(new FilterOption(3L, "Điểm TMDB"));
        sortAdapter = new FilterChipAdapter(sorts, option -> {
            if (option.getName().equals("Mới nhất")) currentSortBy = "createdAt_desc";
            else if (option.getName().equals("Cũ nhất")) currentSortBy = "createdAt_asc";
            else if (option.getName().equals("Điểm TMDB")) currentSortBy = "voteAverage_desc";
            loadMovies();
        });
        rvSorts.setAdapter(sortAdapter);
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
                    genreAdapter = new FilterChipAdapter(options, opt -> {
                        currentGenreId = opt.getId(); loadMovies();
                    });
                    rvGenres.setAdapter(genreAdapter);
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
                    countryAdapter = new FilterChipAdapter(options, opt -> {
                        currentCountryId = opt.getId(); loadMovies();
                    });
                    rvCountries.setAdapter(countryAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<CountryDto>> call, Throwable t) {}
        });

        // Age Ratings
        apiService.getAgeRatings().enqueue(new Callback<List<AgeRatingDto>>() {
            @Override
            public void onResponse(Call<List<AgeRatingDto>> call, Response<List<AgeRatingDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FilterOption> options = new ArrayList<>();
                    options.add(new FilterOption((Long)null, "Tất cả"));
                    for (AgeRatingDto a : response.body()) options.add(new FilterOption(a.getId(), a.getName()));
                    ageAdapter = new FilterChipAdapter(options, opt -> {
                        currentAgeRatingId = opt.getId(); loadMovies();
                    });
                    rvAgeRatings.setAdapter(ageAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<AgeRatingDto>> call, Throwable t) {}
        });
    }

    private void loadMovies() {
        apiService.filterMedia(
                currentKeyword, currentGenreId, currentCountryId, currentAgeRatingId,
                currentReleaseYear, currentMediaType, currentSortBy, 0, 50
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
}
