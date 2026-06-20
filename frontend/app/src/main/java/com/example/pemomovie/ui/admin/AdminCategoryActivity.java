package com.example.pemomovie.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminCategoryAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminGenreDto;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryActivity extends AppCompatActivity {
    private AdminCategoryAdapter adapter;
    private List<AdminGenreDto> allCategories = new ArrayList<>();
    private List<AdminGenreDto> displayedCategories = new ArrayList<>();
    
    private int sortViewsMode = 0; // 0: None, 1: Desc, 2: Asc
    private String searchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_category);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        com.example.pemomovie.utils.AdminNavigationHelper.setupBottomNavigation(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        ImageView btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) btnAddCategory.setOnClickListener(v -> showCreateCategoryDialog());

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminCategoryAdapter(this, new AdminCategoryAdapter.OnCategoryActionListener() {
                @Override
                public void onEdit(AdminGenreDto category) {
                    Intent intent = new Intent(AdminCategoryActivity.this, AdminCategoryDetailActivity.class);
                    intent.putExtra("GENRE_ID", category.getId());
                    intent.putExtra("GENRE_NAME", category.getName());
                    intent.putExtra("GENRE_IS_DELETED", category.isDeleted());
                    startActivity(intent);
                }

                @Override
                public void onDelete(AdminGenreDto category) {
                    handleDeleteCategory(category);
                }
            });
            rvCategories.setAdapter(adapter);
        }

        setupFilters();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupFilters() {
        EditText edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    searchQuery = s.toString().trim().toLowerCase();
                    applyFilters();
                }
            });
        }
        
        MaterialCardView btnSortViews = findViewById(R.id.btnSortViews);
        if (btnSortViews != null) {
            btnSortViews.setOnClickListener(v -> {
                sortViewsMode = (sortViewsMode + 1) % 3;
                updateSortUI(btnSortViews);
                applyFilters();
            });
        }
        
        // Hide the filter button (3rd child)
        View spaceFilter = findViewById(R.id.spaceFilter);
        View btnFilter = findViewById(R.id.btnFilter);
        if (spaceFilter != null) spaceFilter.setVisibility(View.GONE);
        if (btnFilter != null) btnFilter.setVisibility(View.GONE);
    }

    private void updateSortUI(MaterialCardView btnSortViews) {
        boolean isActive = sortViewsMode != 0;
        btnSortViews.setStrokeColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#333333"));
        TextView tv = findViewById(R.id.tvSortViews);
        if (tv != null) {
            tv.setTextColor(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            if (sortViewsMode == 1) tv.setText("Lượt xem: Giảm dần");
            else if (sortViewsMode == 2) tv.setText("Lượt xem: Tăng dần");
            else tv.setText("Sắp xếp: Lượt xem");
        }
        
        ImageView iv = findViewById(R.id.ivSortViews);
        if (iv != null) {
            iv.setColorFilter(android.graphics.Color.parseColor(isActive ? "#D946EF" : "#9CA3AF"));
            iv.setRotation(sortViewsMode == 2 ? -90 : 90);
        }
    }

    private void applyFilters() {
        displayedCategories.clear();
        for (AdminGenreDto dto : allCategories) {
            if (searchQuery.isEmpty() || dto.getName().toLowerCase().contains(searchQuery)) {
                displayedCategories.add(dto);
            }
        }
        
        if (sortViewsMode != 0) {
            displayedCategories.sort((a, b) -> sortViewsMode == 1 ? 
                Integer.compare(b.getViewCount(), a.getViewCount()) : 
                Integer.compare(a.getViewCount(), b.getViewCount()));
        }
        
        if (adapter != null) adapter.setCategories(displayedCategories);
    }

    private void loadData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllCategoriesAdmin().enqueue(new Callback<List<AdminGenreDto>>() {
            @Override
            public void onResponse(Call<List<AdminGenreDto>> call, Response<List<AdminGenreDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories.clear();
                    allCategories.addAll(response.body());
                    applyFilters();
                } else {
                    Toast.makeText(AdminCategoryActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AdminGenreDto>> call, Throwable t) {
                Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateCategoryDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_input_admin);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        EditText etInput = dialog.findViewById(R.id.etDialogInput);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        android.widget.Button btnSave = dialog.findViewById(R.id.btnDialogSave);

        tvTitle.setText("Thêm Thể loại mới");
        etInput.setHint("Nhập tên thể loại...");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etInput.getText().toString().trim();
            if (!name.isEmpty()) {
                dialog.dismiss();
                createCategoryApi(name);
            } else {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void createCategoryApi(String name) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        
        apiService.createCategory(body).enqueue(new Callback<AdminGenreDto>() {
            @Override
            public void onResponse(Call<AdminGenreDto> call, Response<AdminGenreDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminCategoryActivity.this, "Tạo thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminCategoryActivity.this, AdminCategoryDetailActivity.class);
                    intent.putExtra("GENRE_ID", response.body().getId());
                    intent.putExtra("GENRE_NAME", response.body().getName());
                    intent.putExtra("GENRE_IS_DELETED", response.body().isDeleted());
                    startActivity(intent);
                } else {
                    Toast.makeText(AdminCategoryActivity.this, "Lỗi tạo thể loại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminGenreDto> call, Throwable t) {
                Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeleteCategory(AdminGenreDto category) {
        String msg = category.isDeleted() ? 
            "Khôi phục thể loại này sẽ khôi phục lại " + category.getMediaCount() + " phim đã bị ẩn cùng nó. Bạn có chắc chắn?" : 
            "Hiện có " + category.getMediaCount() + " phim đang thuộc thể loại này, xóa đi sẽ khiến các phim đó ngừng hoạt động. Bạn có chắc chắn?";
            
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        android.widget.Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText("Xác nhận");
        tvMessage.setText(msg);

        if (category.isDeleted()) {
            icon.setImageResource(R.drawable.ic_restore); // Restore icon
            icon.setColorFilter(android.graphics.Color.parseColor("#10B981"));
        } else {
            icon.setImageResource(R.drawable.ic_delete); // Delete icon
            icon.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            apiService.softDeleteCategory(category.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminCategoryActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                        loadData(); // Tải lại danh sách
                    } else {
                        Toast.makeText(AdminCategoryActivity.this, "Lỗi thực thi", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(AdminCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
