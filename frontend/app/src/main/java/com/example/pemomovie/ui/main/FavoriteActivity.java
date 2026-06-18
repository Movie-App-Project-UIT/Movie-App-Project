package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.utils.NavigationHelper;

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

        TextView tvHeaderFav = findViewById(R.id.tvHeaderFav);
        GradientTextView.applyHorizontalGradient(
                tvHeaderFav,
                Color.parseColor("#6C29D6"), // tím
                Color.parseColor("#F43393")  // hồng
        );

        // Khởi list với item poster_detail_item
        // Gắn thêm flag để ở Adapter để khi ở màn hình Favorite thì ic_heart luôn ở trạng thái đã nhấn yêu thích
        // Nếu người dùng ấn vào ic_heart thì hiện thông báo Bạn chắc chắn muốn xóa khỏi danh sách yêu thích
        NavigationHelper.setupBottomNavigation(this);
    }
}
