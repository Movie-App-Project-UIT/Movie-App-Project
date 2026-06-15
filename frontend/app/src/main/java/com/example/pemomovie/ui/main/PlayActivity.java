package com.example.pemomovie.ui.main;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pemomovie.R;

public class PlayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Long movieId = getIntent().getLongExtra("MOVIE_ID", -1);
        TextView tvPlayPlaceholder = findViewById(R.id.tvPlayPlaceholder);
        if (movieId != -1) {
            tvPlayPlaceholder.setText("Đang phát phim ID: " + movieId);
        }
    }
}
