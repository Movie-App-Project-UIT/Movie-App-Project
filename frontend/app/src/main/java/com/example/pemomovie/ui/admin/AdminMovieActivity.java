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
    private String filterGenre = "Tất cả";
    private String filterLanguage = "Tất cả";
    private String filterCountry = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_movie);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
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
        apiService.getAllMoviesAdmin().enqueue(new Callback<List<MediaItemDto>>() {
            @Override
            public void onResponse(Call<List<MediaItemDto>> call, Response<List<MediaItemDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allMovies = response.body();
                    filterListByTab();
                } else {
                    Toast.makeText(AdminMovieActivity.this, "Lỗi khi tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MediaItemDto>> call, Throwable t) {
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
                boolean matchGenre = filterGenre.equals("Tất cả") || (movie.getGenres() != null && movie.getGenres().contains(filterGenre));
                boolean matchLanguage = filterLanguage.equals("Tất cả") || (movie.getLanguage() != null && movie.getLanguage().equals(filterLanguage));
                boolean matchCountry = filterCountry.equals("Tất cả") || (movie.getCountry() != null && movie.getCountry().equals(filterCountry));
                
                if (matchGenre && matchLanguage && matchCountry) {
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
        
        android.widget.Spinner spinnerGenre = dialog.findViewById(R.id.spinnerGenre);
        android.widget.Spinner spinnerLanguage = dialog.findViewById(R.id.spinnerLanguage);
        android.widget.Spinner spinnerCountry = dialog.findViewById(R.id.spinnerCountry);
        
        java.util.Set<String> genres = new java.util.LinkedHashSet<>();
        java.util.Set<String> languages = new java.util.LinkedHashSet<>();
        java.util.Set<String> countries = new java.util.LinkedHashSet<>();
        
        genres.add("Tất cả");
        languages.add("Tất cả");
        countries.add("Tất cả");
        
        for (MediaItemDto m : allMovies) {
            if (m.getGenres() != null) genres.addAll(m.getGenres());
            if (m.getLanguage() != null && !m.getLanguage().equals("N/A")) languages.add(m.getLanguage());
            if (m.getCountry() != null && !m.getCountry().equals("N/A")) countries.add(m.getCountry());
        }
        
        java.util.List<String> genreList = new java.util.ArrayList<>(genres);
        java.util.List<String> languageList = new java.util.ArrayList<>(languages);
        java.util.List<String> countryList = new java.util.ArrayList<>(countries);
        
        android.widget.ArrayAdapter<String> genreAdapter = new android.widget.ArrayAdapter<>(this, R.layout.item_spinner, genreList);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        android.widget.ArrayAdapter<String> languageAdapter = new android.widget.ArrayAdapter<>(this, R.layout.item_spinner, languageList);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        android.widget.ArrayAdapter<String> countryAdapter = new android.widget.ArrayAdapter<>(this, R.layout.item_spinner, countryList);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinnerGenre.setAdapter(genreAdapter);
        spinnerLanguage.setAdapter(languageAdapter);
        spinnerCountry.setAdapter(countryAdapter);
        
        spinnerGenre.setSelection(Math.max(0, genreList.indexOf(filterGenre)));
        spinnerLanguage.setSelection(Math.max(0, languageList.indexOf(filterLanguage)));
        spinnerCountry.setSelection(Math.max(0, countryList.indexOf(filterCountry)));
        
        dialog.findViewById(R.id.btnFilterClear).setOnClickListener(v -> {
            filterGenre = "Tất cả";
            filterLanguage = "Tất cả";
            filterCountry = "Tất cả";
            filterListByTab();
            dialog.dismiss();
        });
        
        dialog.findViewById(R.id.btnFilterApply).setOnClickListener(v -> {
            filterGenre = spinnerGenre.getSelectedItem().toString();
            filterLanguage = spinnerLanguage.getSelectedItem().toString();
            filterCountry = spinnerCountry.getSelectedItem().toString();
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
