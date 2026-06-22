package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.stream.Collectors;
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
    private CheckBox cbFilterReported;
    private AdminMovieReviewAdapter adapter;
    private ApiService apiService;
    private List<ReviewResponseDto> allReviews;

    public AdminMovieReviewBottomSheet(Long movieId) {
        this.movieId = movieId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_admin_movie_review, container, false);
        
        rvReviews = view.findViewById(R.id.rvReviews);
        progressBar = view.findViewById(R.id.progressBar);
        cbFilterReported = view.findViewById(R.id.cbFilterReported);
        
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminMovieReviewAdapter();
        rvReviews.setAdapter(adapter);
        
        apiService = ApiClient.getApiService();
        
        cbFilterReported.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyFilter();
        });
        
        fetchReviews();
        
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        android.app.Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }
    
    private void fetchReviews() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getReviewsByMediaForAdmin(movieId).enqueue(new Callback<List<ReviewResponseDto>>() {
            @Override
            public void onResponse(Call<List<ReviewResponseDto>> call, Response<List<ReviewResponseDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allReviews = new java.util.ArrayList<>();
                    for (ReviewResponseDto root : response.body()) {
                        allReviews.add(root);
                        flattenReplies(allReviews, root.getReplies());
                    }
                    if (allReviews.isEmpty()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Chưa có bình luận nào", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        applyFilter();
                        if (getContext() != null) {
                            TextView tvTitle = getView() != null ? getView().findViewById(R.id.tvTitle) : null;
                            if (tvTitle != null) {
                                tvTitle.setText("Danh sách bình luận (" + allReviews.size() + ")");
                            }
                        }
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

    private void flattenReplies(java.util.List<ReviewResponseDto> targetList, java.util.List<ReviewResponseDto> replies) {
        if (replies == null) return;
        for (ReviewResponseDto r : replies) {
            targetList.add(r);
            flattenReplies(targetList, r.getReplies());
        }
    }

    private void applyFilter() {
        if (allReviews == null) return;
        List<ReviewResponseDto> filteredList = new java.util.ArrayList<>(allReviews);
        if (cbFilterReported.isChecked()) {
            filteredList = filteredList.stream()
                .filter(r -> r.getReportCount() > 0)
                .sorted((r1, r2) -> Long.compare(r2.getReportCount(), r1.getReportCount()))
                .collect(Collectors.toList());
        }
        adapter.setReviews(filteredList);
    }
}
