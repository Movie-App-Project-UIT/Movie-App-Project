package com.example.pemomovie.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.pemomovie.R;
import com.example.pemomovie.custom.GradientTextView;
import com.example.pemomovie.utils.NavigationHelper;

public class MovieActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NavigationHelper.setupBottomNavigation(this);

        Button btnSearch = findViewById(R.id.btnSearch);

        GradientTextView.applyHorizontalGradient(
                btnSearch,
                Color.parseColor("#6C29D6"),
                Color.parseColor("#F43393")
        );
    }

    // mặc định sẽ nằm ở tab tất cả lúc sang màn hình
    // phải luôn có tab Tất cả + thể loại phim có trong app
    // Khi click vào 1 trong các tab đó sẽ hiển thị 1 list poster_genre_item
    //Khi ở tab Tất cả hía dưới sẽ hiển thị list section tương tự ở màn Home
    //Và Title của các section sẽ phải có là "Đề xuất" của hệ thống và các section còn lại là các thể loại có trong app + Phim mới ...

}
