package com.example.pemomovie;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //tạo màu linear cho IMDb
        TextView txtIMDb = findViewById(R.id.txt_IMDb);
        Shader textShader = new LinearGradient(0, 0, 0, txtIMDb.getTextSize(),
                new int[]{Color.parseColor("#6C29D6"), Color.parseColor("#F43393")}, null, Shader.TileMode.CLAMP);

        txtIMDb.getPaint().setShader(textShader);
    }
}