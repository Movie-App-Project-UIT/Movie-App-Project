package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.adapter.FavoriteAdapter;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.utils.FavoriteManager;
import com.example.pemomovie.utils.NavigationHelper;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView rvFavorites = findViewById(R.id.rvFav);
        TextView tvHeaderFav = findViewById(R.id.tvHeaderFav);
        GradientTextView.applyHorizontalGradient(
                tvHeaderFav,
                Color.parseColor("#6C29D6"), // tím
                Color.parseColor("#F43393")  // hồng
        );

        // Cài đặt thanh bottom navigation
        NavigationHelper.setupBottomNavigation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        RecyclerView rvFavorites = findViewById(R.id.rvFav);
        rvFavorites.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        
        // Lấy danh sách yêu thích và cập nhật adapter
        List<MediaItemDto> favList = FavoriteManager.getFavorites(this);
        FavoriteAdapter adapter = new FavoriteAdapter(favList);
        rvFavorites.setAdapter(adapter);

        // Cập nhật số lượng phim
        TextView tvCountMovieFav = findViewById(R.id.tvCountMovieFav);
        if (tvCountMovieFav != null) {
            tvCountMovieFav.setText(favList.size() + " phim");
        }
    }
}
