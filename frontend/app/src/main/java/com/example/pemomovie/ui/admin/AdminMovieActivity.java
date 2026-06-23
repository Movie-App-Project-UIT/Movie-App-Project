package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminMovieAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaItemDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class AdminMovieActivity extends AppCompatActivity {

    private RecyclerView rvAdminMovies;
    private ProgressBar progressBar;
    private AdminMovieAdapter adapter;
    private ApiService apiService;
    
    private boolean isShowingInactive = false;
    private List<MediaItemDto> allMovies;
    private TextView tabActive, tabInactive;
    
    private int sortViewsMode = 0; // 0: None, 1: Desc, 2: Asc
    private int sortRatingMode = 0; // 0: None, 1: Desc, 2: Asc
    private java.util.Set<String> filterGenres = new java.util.HashSet<>();
    private java.util.Set<String> filterLanguages = new java.util.HashSet<>();
    private java.util.Set<String> filterCountries = new java.util.HashSet<>();
    private Boolean filterIsPremium = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_movie);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            View header = findViewById(R.id.layoutHeader);
            if (header != null) {
                header.setPadding(header.getPaddingLeft(), systemBars.top, header.getPaddingRight(), header.getPaddingBottom());
            }
            
            View bottomNav = findViewById(R.id.adminBottomNavInclude);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }
            return insets;
        });

        rvAdminMovies = findViewById(R.id.rvAdminMovies);
        progressBar = findViewById(R.id.progressBar);
        progressBar = findViewById(R.id.progressBar);
        ImageView btnAddMovie = findViewById(R.id.btnAddMovie);

        apiService = ApiClient.getApiService();
        
        tabActive = findViewById(R.id.tabActive);
        tabInactive = findViewById(R.id.tabInactive);
        
        tabActive.setOnClickListener(v -> switchTab(false));
        tabInactive.setOnClickListener(v -> switchTab(true));
        
        switchTab(false); // Khởi tạo giao diện tab mặc định
        
        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (adapter != null) {
                        adapter.getFilter().filter(s.toString());
                    }
                }
            });
        }

        View btnSortViews = findViewById(R.id.btnSortViews);
        View btnSortRating = findViewById(R.id.btnSortRating);
        
        if (btnSortViews != null) {
            btnSortViews.setOnClickListener(v -> {
                sortViewsMode = (sortViewsMode + 1) % 3;
                sortRatingMode = 0;
                updateSortUI();
                filterListByTab();
            });
        }

        if (btnSortRating != null) {
            btnSortRating.setOnClickListener(v -> {
                sortRatingMode = (sortRatingMode + 1) % 3;
                sortViewsMode = 0;
                updateSortUI();
                filterListByTab();
            });
        }

        View btnFilter = findViewById(R.id.btnFilter);
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterDialog());
        }

        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminMovieAddActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMovies();
    }

    private void setupRecyclerView() {
        adapter = new AdminMovieAdapter(this, new AdminMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItemDto movie) {
                Intent intent = new Intent(AdminMovieActivity.this, AdminMovieDetailActivity.class);
                intent.putExtra("MOVIE_ID", movie.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(MediaItemDto movie) {
                showConfirmDialog(movie);
            }
        });
        rvAdminMovies.setLayoutManager(new LinearLayoutManager(this));
        rvAdminMovies.setAdapter(adapter);
    }
    
    private void showConfirmDialog(MediaItemDto movie) {
        if (isShowingInactive && movie.getHiddenByGenreId() != null) {
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.dialog_confirm);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            
            TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
            TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
            ImageView icon = dialog.findViewById(R.id.dialogIcon);
            
            tvTitle.setText("Không thể khôi phục");
            tvMessage.setText("Phim bị ẩn bởi thể loại phim, hãy mở lại hoạt động cho thể loại phim trước");
            
            icon.setImageResource(R.drawable.ic_lock); // Hoặc icon khác
            icon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
            
            dialog.findViewById(R.id.btnDialogCancel).setVisibility(View.GONE);
            android.widget.Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);
            btnConfirm.setText("Đóng");
            btnConfirm.setOnClickListener(v -> dialog.dismiss());
            
            dialog.show();
            return;
        }

        String title = isShowingInactive ? "Khôi phục phim" : "Ẩn phim";
        String message = isShowingInactive ? "Bạn có chắc chắn muốn khôi phục phim này không?" : "Bạn có chắc chắn muốn ẩn phim này khỏi hệ thống không?";

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        
        tvTitle.setText(title);
        tvMessage.setText(message);
        
        if (isShowingInactive) {
            icon.setImageResource(R.drawable.ic_save); // Restore icon
            icon.setColorFilter(android.graphics.Color.parseColor("#10B981"));
        } else {
            icon.setImageResource(R.drawable.ic_delete); // Delete icon
            icon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
        }
        
        dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            softDeleteMovie(movie);
        });
        
        dialog.show();
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAllMoviesAdmin(0, 1000).enqueue(new Callback<com.example.pemomovie.dto.PageResponseDto<MediaItemDto>>() {
            @Override
            public void onResponse(Call<com.example.pemomovie.dto.PageResponseDto<MediaItemDto>> call, Response<com.example.pemomovie.dto.PageResponseDto<MediaItemDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allMovies = response.body().getContent();
                    filterListByTab();
                } else {
                    Toast.makeText(AdminMovieActivity.this, "Lỗi khi tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.pemomovie.dto.PageResponseDto<MediaItemDto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminMovieActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void softDeleteMovie(MediaItemDto movie) {
        // Optimistic UI Update: thay đổi local trước để UI update tức thời
        movie.setDeleted(!movie.isDeleted());
        filterListByTab(); // Lọc lại danh sách ngay lập tức để phim mất khỏi màn hình hiện tại
        
        apiService.softDeleteMovie(movie.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminMovieActivity.this, "Thành công!", Toast.LENGTH_SHORT).show();
                    // Đã update local nên không cần loadMovies() trừ khi muốn đồng bộ lại 100% với backend
                } else {
                    // Nếu lỗi, hoàn tác lại thay đổi local
                    movie.setDeleted(!movie.isDeleted());
                    filterListByTab();
                    Toast.makeText(AdminMovieActivity.this, "Lỗi khi cập nhật phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Nếu lỗi, hoàn tác lại
                movie.setDeleted(!movie.isDeleted());
                filterListByTab();
                Toast.makeText(AdminMovieActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchTab(boolean showInactive) {
        isShowingInactive = showInactive;
        
        // Update UI colors and backgrounds
        tabActive.setTextColor(android.graphics.Color.parseColor(showInactive ? "#9CA3AF" : "#FFFFFF"));
        tabInactive.setTextColor(android.graphics.Color.parseColor(showInactive ? "#FFFFFF" : "#9CA3AF"));
        
        tabActive.setBackgroundResource(showInactive ? R.drawable.bg_tab_inactive : R.drawable.bg_tab_active_gradient);
        tabInactive.setBackgroundResource(showInactive ? R.drawable.bg_tab_active_gradient : R.drawable.bg_tab_inactive);

        // Update adapter mode
        if (adapter != null) {
            adapter.setInactiveTab(showInactive);
        }
        
        // Filter list
        filterListByTab();
    }

    private void filterListByTab() {
        if (allMovies == null) return;
        List<MediaItemDto> filtered = new java.util.ArrayList<>();
        for (MediaItemDto movie : allMovies) {
            if (movie.isDeleted() == isShowingInactive) {
                boolean matchGenre = filterGenres.isEmpty() || 
                    (movie.getGenres() != null && movie.getGenres().stream().anyMatch(filterGenres::contains));
                boolean matchLanguage = filterLanguages.isEmpty() || 
                    (movie.getLanguage() != null && filterLanguages.contains(movie.getLanguage()));
                boolean matchCountry = filterCountries.isEmpty() || 
                    (movie.getCountry() != null && filterCountries.contains(movie.getCountry()));
                boolean matchPremium = filterIsPremium == null || (movie.isPremium() == filterIsPremium);
                
                if (matchGenre && matchLanguage && matchCountry && matchPremium) {
                    filtered.add(movie);
                }
            }
        }
        
        // Apply Sorting
        if (sortViewsMode != 0) {
            filtered.sort((m1, m2) -> sortViewsMode == 1 ? 
                Integer.compare(m2.getViewCount(), m1.getViewCount()) : 
                Integer.compare(m1.getViewCount(), m2.getViewCount())
            );
        } else if (sortRatingMode != 0) {
            filtered.sort((m1, m2) -> sortRatingMode == 1 ? 
                Float.compare(m2.getVoteAverage() != null ? m2.getVoteAverage() : 0f, m1.getVoteAverage() != null ? m1.getVoteAverage() : 0f) : 
                Float.compare(m1.getVoteAverage() != null ? m1.getVoteAverage() : 0f, m2.getVoteAverage() != null ? m2.getVoteAverage() : 0f)
            );
        }

        if (adapter != null) {
            adapter.setMovies(filtered);
        }
    }

    private void showFilterDialog() {
        if (allMovies == null) return;
        
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_admin_movie_filter);
        
        RecyclerView rvGenres = dialog.findViewById(R.id.rvGenres);
        RecyclerView rvLanguages = dialog.findViewById(R.id.rvLanguages);
        RecyclerView rvCountries = dialog.findViewById(R.id.rvCountries);
        RecyclerView rvPremium = dialog.findViewById(R.id.rvPremium);
        
        java.util.Set<String> genres = new java.util.LinkedHashSet<>();
        java.util.Set<String> languages = new java.util.LinkedHashSet<>();
        java.util.Set<String> countries = new java.util.LinkedHashSet<>();
        
        for (MediaItemDto m : allMovies) {
            if (m.getGenres() != null) genres.addAll(m.getGenres());
            if (m.getLanguage() != null && !m.getLanguage().equals("N/A")) languages.add(m.getLanguage());
            if (m.getCountry() != null && !m.getCountry().equals("N/A")) countries.add(m.getCountry());
        }
        
        // Setup Genre Adapter
        List<com.example.pemomovie.dto.FilterOption> genreOpts = new java.util.ArrayList<>();
        genreOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, "Tất cả"));
        for (String g : genres) genreOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, g));
        
        com.example.pemomovie.adapter.MultiSelectFilterChipAdapter genreAdapter = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(genreOpts, opts -> {
            filterGenres.clear();
            for (com.example.pemomovie.dto.FilterOption opt : opts) {
                if (!opt.getName().equals("Tất cả")) filterGenres.add(opt.getName());
            }
        });
        rvGenres.setAdapter(genreAdapter);

        // Setup Language Adapter
        List<com.example.pemomovie.dto.FilterOption> langOpts = new java.util.ArrayList<>();
        langOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, "Tất cả"));
        for (String l : languages) langOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, l));
        
        com.example.pemomovie.adapter.MultiSelectFilterChipAdapter langAdapter = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(langOpts, opts -> {
            filterLanguages.clear();
            for (com.example.pemomovie.dto.FilterOption opt : opts) {
                if (!opt.getName().equals("Tất cả")) filterLanguages.add(opt.getName());
            }
        });
        rvLanguages.setAdapter(langAdapter);

        // Setup Country Adapter
        List<com.example.pemomovie.dto.FilterOption> countryOpts = new java.util.ArrayList<>();
        countryOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, "Tất cả"));
        for (String c : countries) countryOpts.add(new com.example.pemomovie.dto.FilterOption((Long) null, c));
        
        com.example.pemomovie.adapter.MultiSelectFilterChipAdapter countryAdapter = new com.example.pemomovie.adapter.MultiSelectFilterChipAdapter(countryOpts, opts -> {
            filterCountries.clear();
            for (com.example.pemomovie.dto.FilterOption opt : opts) {
                if (!opt.getName().equals("Tất cả")) filterCountries.add(opt.getName());
            }
        });
        rvCountries.setAdapter(countryAdapter);

        // Setup Premium Adapter
        List<com.example.pemomovie.dto.FilterOption> premiumOpts = new java.util.ArrayList<>();
        premiumOpts.add(new com.example.pemomovie.dto.FilterOption(0L, "Tất cả"));
        premiumOpts.add(new com.example.pemomovie.dto.FilterOption(1L, "Premium"));
        premiumOpts.add(new com.example.pemomovie.dto.FilterOption(2L, "Miễn phí"));
        com.example.pemomovie.adapter.FilterChipAdapter premiumAdapter = new com.example.pemomovie.adapter.FilterChipAdapter(premiumOpts, opt -> {
            if (opt.getName().equals("Premium")) filterIsPremium = true;
            else if (opt.getName().equals("Miễn phí")) filterIsPremium = false;
            else filterIsPremium = null;
        });
        rvPremium.setAdapter(premiumAdapter);
        
        dialog.findViewById(R.id.btnFilterClear).setOnClickListener(v -> {
            filterGenres.clear();
            filterLanguages.clear();
            filterCountries.clear();
            filterIsPremium = null;
            filterListByTab();
            dialog.dismiss();
        });
        
        dialog.findViewById(R.id.btnFilterApply).setOnClickListener(v -> {
            filterListByTab();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void updateSortUI() {
        com.google.android.material.card.MaterialCardView btnSortViews = findViewById(R.id.btnSortViews);
        com.google.android.material.card.MaterialCardView btnSortRating = findViewById(R.id.btnSortRating);
        
        if (btnSortViews != null) {
            boolean isActive = sortViewsMode != 0;
            btnSortViews.setStrokeColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#333333"));
            TextView tv = (TextView) ((android.view.ViewGroup) btnSortViews.getChildAt(0)).getChildAt(0);
            tv.setTextColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            if (sortViewsMode == 1) tv.setText("Lượt xem: Giảm dần");
            else if (sortViewsMode == 2) tv.setText("Lượt xem: Tăng dần");
            else tv.setText("Sắp xếp: Lượt xem");
            
            ImageView iv = (ImageView) ((android.view.ViewGroup) btnSortViews.getChildAt(0)).getChildAt(1);
            iv.setColorFilter(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            iv.setRotation(sortViewsMode == 2 ? -90 : 90);
        }
        
        if (btnSortRating != null) {
            boolean isActive = sortRatingMode != 0;
            btnSortRating.setStrokeColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#333333"));
            TextView tv = (TextView) ((android.view.ViewGroup) btnSortRating.getChildAt(0)).getChildAt(0);
            tv.setTextColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            if (sortRatingMode == 1) tv.setText("Đánh giá: Giảm dần");
            else if (sortRatingMode == 2) tv.setText("Đánh giá: Tăng dần");
            else tv.setText("Sắp xếp: Điểm đánh giá");
            
            ImageView iv = (ImageView) ((android.view.ViewGroup) btnSortRating.getChildAt(0)).getChildAt(1);
            iv.setColorFilter(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            iv.setRotation(sortRatingMode == 2 ? -90 : 90);
        }
    }
}
