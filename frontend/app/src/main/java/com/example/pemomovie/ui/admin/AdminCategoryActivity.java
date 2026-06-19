package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminCategoryAdapter;
import com.example.pemomovie.mockdata.MockDataHelper;

public class AdminCategoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        ImageView btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> {
                Toast.makeText(this, "Thêm Thể loại mới", Toast.LENGTH_SHORT).show();
            });
        }

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(this));
            AdminCategoryAdapter adapter = new AdminCategoryAdapter(this);
            adapter.setCategories(MockDataHelper.getMockCategories());
            rvCategories.setAdapter(adapter);
        }
    }
}
