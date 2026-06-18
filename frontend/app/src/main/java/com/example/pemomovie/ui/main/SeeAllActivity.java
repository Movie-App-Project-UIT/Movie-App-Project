package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.PosterAdapter;
import com.example.pemomovie.custom.GradientTextView;

public class SeeAllActivity extends AppCompatActivity {

    private RecyclerView rvMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_all);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvSectionTitle = findViewById(R.id.tvSectionTitle);

        // Lấy tiêu đề từ Intent (ví dụ: "Phim mới", "Đang thịnh hành")
        String sectionTitle = getIntent().getStringExtra("SECTION_TITLE");
        if (sectionTitle != null) {
            tvSectionTitle.setText(sectionTitle);
        }

        GradientTextView.applyHorizontalGradient(
                tvSectionTitle,
                Color.parseColor("#6C29D6"), // tím
                Color.parseColor("#F43393")  // hồng
        );

        //list phim theo hàng dọc mỗi hàng chứa 2 poster_detail_item
        //rvMovies.setLayoutManager(new GridLayoutManager(this, 2));

        //Nếu click vào ic_heart thì thêm phim vào danh sách yêu thích

    }
}