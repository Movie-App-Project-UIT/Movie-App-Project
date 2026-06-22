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

    private EditText edtSearch;
    private LinearLayout btnSortOptions;
    private TextView txtCurrentSort;

    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private TabLayout tabLayoutMediaType;
    private LinearLayout btnToggleFilter, layoutFiltersContainer;
    private TextView txtToggleFilter;

    private RecyclerView rvCountries, rvGenres, rvYears, rvMovies;

    private FilterChipAdapter countryAdapter, genreAdapter, yearAdapter;
    private PosterAdapter movieAdapter;

    private ApiService apiService;

    // Filter State
    private String currentKeyword = null;
    private String currentMediaType = null; // null = Tất cả
    private Long currentGenreId = null;
    private Long currentCountryId = null;
    private Long currentAgeRatingId = null;
    private Integer currentReleaseYear = null;
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
        setupSearch();

        setupStaticFilters();
        loadDynamicFilters();

        // Initial load
        loadMovies();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);

        tabLayoutMediaType = findViewById(R.id.tabLayoutMediaType);
        btnToggleFilter = findViewById(R.id.btnToggleFilter);
        layoutFiltersContainer = findViewById(R.id.layoutFiltersContainer);
        txtToggleFilter = findViewById(R.id.txtToggleFilter);

        rvCountries = findViewById(R.id.rvCountries);
        rvGenres = findViewById(R.id.rvGenres);
        rvYears = findViewById(R.id.rvYears);
        rvMovies = findViewById(R.id.rvMovies);

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
                    case 0:
                        currentMediaType = null;
                        break; // Tất cả
                    case 1:
                        currentMediaType = "MOVIE";
                        break; // Phim Lẻ
                    case 2:
                        currentMediaType = "TV_SERIES";
                        break; // Phim Bộ
                }
                loadMovies();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentKeyword = s.toString().trim();
                    if (currentKeyword.isEmpty())
                        currentKeyword = null;
                    loadMovies();
                };
                searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
            }
        });
    }

    private void setupStaticFilters() {
        // Years
        List<FilterOption> years = new ArrayList<>();
        years.add(new FilterOption((Integer) null, "Tất cả"));
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

                final android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(popupView, width, height,
                        focusable);
                popupWindow.setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
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
                    options.add(new FilterOption((Long) null, "Tất cả"));
                    for (GenreDto g : response.body())
                        options.add(new FilterOption(g.getId(), g.getName()));
                    genreAdapter = new FilterChipAdapter(options, opt -> {
                        currentGenreId = opt.getId();
                        loadMovies();
                    });
                    rvGenres.setAdapter(genreAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<GenreDto>> call, Throwable t) {
            }
        });

        // Countries
        apiService.getCountries().enqueue(new Callback<List<CountryDto>>() {
            @Override
            public void onResponse(Call<List<CountryDto>> call, Response<List<CountryDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FilterOption> options = new ArrayList<>();
                    options.add(new FilterOption((Long) null, "Tất cả"));
                    for (CountryDto c : response.body())
                        options.add(new FilterOption(c.getId(), c.getName()));
                    countryAdapter = new FilterChipAdapter(options, opt -> {
                        currentCountryId = opt.getId();
                        loadMovies();
                    });
                    rvCountries.setAdapter(countryAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<CountryDto>> call, Throwable t) {
            }
        });

        // Age Ratings đã bị ẩn
    }

    private void loadMovies() {
        apiService.filterMedia(
                currentKeyword, currentGenreId, currentCountryId, currentAgeRatingId,
                currentReleaseYear, currentMediaType, currentSortBy, 0, 50)
                .enqueue(new Callback<PageResponseDto<MediaItemDto>>() {
                    @Override
                    public void onResponse(Call<PageResponseDto<MediaItemDto>> call,
                            Response<PageResponseDto<MediaItemDto>> response) {
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
