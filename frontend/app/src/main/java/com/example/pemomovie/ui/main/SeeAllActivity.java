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
import com.example.pemomovie.adapter.SeeAllAdapter;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.dto.MediaItemDto;

import java.util.ArrayList;
import java.util.List;

public class SeeAllActivity extends AppCompatActivity {

    private RecyclerView rvTitle;

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

        // Tiêu đề section
        TextView tvSectionTitle = findViewById(R.id.tvSectionTitle);
        String sectionTitle = getIntent().getStringExtra("SECTION_TITLE");
        if (sectionTitle != null) {
            tvSectionTitle.setText(sectionTitle);
        }
        GradientTextView.applyHorizontalGradient(
                tvSectionTitle,
                Color.parseColor("#6C29D6"), // tím
                Color.parseColor("#F43393")  // hồng
        );

        // Nhận danh sách phim từ Intent
        @SuppressWarnings("unchecked")
        List<MediaItemDto> movieList = (ArrayList<MediaItemDto>)
                getIntent().getSerializableExtra("MOVIE_LIST");
        if (movieList == null) {
            movieList = new ArrayList<>();
        }

        // Hiển thị Grid 2 cột dùng SeeAllAdapter (poster_detail_item)
        rvTitle = findViewById(R.id.rvTitle);
        rvTitle.setLayoutManager(new GridLayoutManager(this, 2));
        SeeAllAdapter adapter = new SeeAllAdapter(this, movieList);
        rvTitle.setAdapter(adapter);
    }
}