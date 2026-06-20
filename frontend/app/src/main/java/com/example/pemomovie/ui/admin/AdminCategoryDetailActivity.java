package com.example.pemomovie.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminMovieAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaItemDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryDetailActivity extends AppCompatActivity {

    private AdminMovieAdapter adapter;
    private Long genreId;
    private String genreName;
    private android.widget.CompoundButton.OnCheckedChangeListener switchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_category_detail);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        genreId = getIntent().getLongExtra("GENRE_ID", -1);
        genreName = getIntent().getStringExtra("GENRE_NAME");

        if (genreId == -1) {
            Toast.makeText(this, "Lỗi id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvCategoryName = findViewById(R.id.tvCategoryName);
        if (tvCategoryName != null && genreName != null) {
            tvCategoryName.setText("Chi tiết: " + genreName);
        }
        
        boolean isDeleted = getIntent().getBooleanExtra("GENRE_IS_DELETED", false);
        androidx.appcompat.widget.SwitchCompat switchStatus = findViewById(R.id.switchStatus);
        TextView tvStatusDesc = findViewById(R.id.tvStatusDesc);
        
        if (switchStatus != null) {
            switchStatus.setChecked(!isDeleted);
            if (tvStatusDesc != null) {
                tvStatusDesc.setText(!isDeleted ? "Thể loại đang được hiển thị" : "Thể loại đang bị ẩn");
            }
            
            switchListener = (buttonView, isChecked) -> {
                if (tvStatusDesc != null) {
                    tvStatusDesc.setText(isChecked ? "Thể loại đang được hiển thị" : "Thể loại đang bị ẩn");
                }
                
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                apiService.softDeleteCategory(genreId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminCategoryDetailActivity.this, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                            switchStatus.setOnCheckedChangeListener(null);
                            switchStatus.setChecked(!isChecked);
                            switchStatus.setOnCheckedChangeListener(switchListener);
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        switchStatus.setOnCheckedChangeListener(null);
                        switchStatus.setChecked(!isChecked);
                        switchStatus.setOnCheckedChangeListener(switchListener);
                    }
                });
            };
            
            switchStatus.setOnCheckedChangeListener(switchListener);
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        ImageView btnAddMedia = findViewById(R.id.btnAddMedia);
        if (btnAddMedia != null) btnAddMedia.setOnClickListener(v -> openAddMediaDialog());

        RecyclerView rvMedia = findViewById(R.id.rvMedia);
        if (rvMedia != null) {
            rvMedia.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminMovieAdapter(this, new AdminMovieAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(MediaItemDto movie) {
                    // Xem chi tiết phim
                }

                @Override
                public void onDeleteClick(MediaItemDto movie) {
                    removeMediaFromGenre(movie);
                }
            });
            adapter.setHideEditButton(true);
            rvMedia.setAdapter(adapter);
        }

        loadMediaInGenre();
    }

    private void loadMediaInGenre() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getMediaInGenre(genreId).enqueue(new Callback<List<MediaItemDto>>() {
            @Override
            public void onResponse(Call<List<MediaItemDto>> call, Response<List<MediaItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMovies(response.body());
                } else {
                    Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi tải phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MediaItemDto>> call, Throwable t) {
                Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMediaFromGenre(MediaItemDto movie) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        
        tvTitle.setText("Gỡ bỏ phim");
        tvMessage.setText("Bạn có chắc chắn muốn gỡ phim '" + movie.getTitle() + "' khỏi thể loại này?");
        
        icon.setImageResource(R.drawable.ic_delete); // Delete icon
        icon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
        
        dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.removeMediaFromGenre(genreId, movie.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminCategoryDetailActivity.this, "Gỡ thành công", Toast.LENGTH_SHORT).show();
                        loadMediaInGenre(); // Tải lại danh sách
                    } else {
                        Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi thực thi", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
    }

    private void openAddMediaDialog() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getMediaNotInGenre(genreId).enqueue(new Callback<List<MediaItemDto>>() {
            @Override
            public void onResponse(Call<List<MediaItemDto>> call, Response<List<MediaItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSelectMediaDialog(response.body());
                } else {
                    Toast.makeText(AdminCategoryDetailActivity.this, "Không thể tải danh sách phim", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MediaItemDto>> call, Throwable t) {
                Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSelectMediaDialog(List<MediaItemDto> availableMedia) {
        if (availableMedia.isEmpty()) {
            Toast.makeText(this, "Không có phim nào để thêm", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_admin_select_movie);
        
        RecyclerView rvSelectMovies = bottomSheetDialog.findViewById(R.id.rvSelectMovies);
        android.widget.Button btnClose = bottomSheetDialog.findViewById(R.id.btnClose);
        
        if (rvSelectMovies != null) {
            rvSelectMovies.setLayoutManager(new LinearLayoutManager(this));
            AdminMovieAdapter selectAdapter = new AdminMovieAdapter(this, new AdminMovieAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(MediaItemDto movie) {
                    bottomSheetDialog.dismiss();
                    addMediaToGenre(movie);
                }

                @Override
                public void onDeleteClick(MediaItemDto movie) {
                    // Do nothing
                }
            });
            selectAdapter.setHideEditButton(true);
            selectAdapter.setHideDeleteButton(true);
            selectAdapter.setMovies(availableMedia);
            rvSelectMovies.setAdapter(selectAdapter);
        }
        
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void addMediaToGenre(MediaItemDto movie) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.addMediaToGenre(genreId, movie.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCategoryDetailActivity.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    loadMediaInGenre();
                } else {
                    Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi thực thi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
