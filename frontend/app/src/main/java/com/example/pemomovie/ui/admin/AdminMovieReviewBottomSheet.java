package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminMovieReviewAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.ReviewResponseDto;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMovieReviewBottomSheet extends BottomSheetDialogFragment {

    private Long movieId;
    private RecyclerView rvReviews;
    private ProgressBar progressBar;
    private AdminMovieReviewAdapter adapter;
    private ApiService apiService;

    public AdminMovieReviewBottomSheet(Long movieId) {
        this.movieId = movieId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_admin_movie_review, container, false);
        
        rvReviews = view.findViewById(R.id.rvReviews);
        progressBar = view.findViewById(R.id.progressBar);
        
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminMovieReviewAdapter();
        rvReviews.setAdapter(adapter);
        
        apiService = ApiClient.getApiService();
        
        fetchReviews();
        
        return view;
    }
    
    private void fetchReviews() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getReviewsByMedia(movieId).enqueue(new Callback<List<ReviewResponseDto>>() {
            @Override
            public void onResponse(Call<List<ReviewResponseDto>> call, Response<List<ReviewResponseDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ReviewResponseDto> reviews = response.body();
                    if (reviews.isEmpty()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Chưa có bình luận nào", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        adapter.setReviews(reviews);
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải danh sách bình luận", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ReviewResponseDto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
