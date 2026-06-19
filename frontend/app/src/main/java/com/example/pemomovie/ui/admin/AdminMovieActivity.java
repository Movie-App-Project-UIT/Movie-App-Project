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

public class AdminMovieActivity extends AppCompatActivity {

    private RecyclerView rvAdminMovies;
    private ProgressBar progressBar;
    private AdminMovieAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        apiService = ApiClient.getApiService();
        
        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);

        btnAddMovie.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm phim mới sắp ra mắt", Toast.LENGTH_SHORT).show();
            // TODO: Open AdminMovieDetailActivity to add a new movie
        });

        setupRecyclerView();
        loadMovies();
    }

    private void setupRecyclerView() {
        adapter = new AdminMovieAdapter(this, new AdminMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MediaItemDto movie) {
                Toast.makeText(AdminMovieActivity.this, "Sửa phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Open AdminMovieDetailActivity for edit
            }

            @Override
            public void onDeleteClick(MediaItemDto movie) {
                softDeleteMovie(movie);
            }
        });
        rvAdminMovies.setLayoutManager(new LinearLayoutManager(this));
        rvAdminMovies.setAdapter(adapter);
    }

    private void loadMovies() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getAllMoviesAdmin().enqueue(new Callback<List<MediaItemDto>>() {
            @Override
            public void onResponse(Call<List<MediaItemDto>> call, Response<List<MediaItemDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMovies(response.body());
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
        apiService.softDeleteMovie(movie.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminMovieActivity.this, "Thành công!", Toast.LENGTH_SHORT).show();
                    loadMovies(); // Reload list to update status
                } else {
                    Toast.makeText(AdminMovieActivity.this, "Lỗi khi xóa/khôi phục phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminMovieActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
