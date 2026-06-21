package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

public class PaymentProcessingActivity extends AppCompatActivity {

    private int selectedPlan = 6;
    private String planPriceStr = "249.000đ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_processing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedPlan = getIntent().getIntExtra("SELECTED_PLAN", 6);
        planPriceStr = getIntent().getStringExtra("PLAN_PRICE");

        // Giả lập quá trình xác nhận thanh toán mất 3 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(PaymentProcessingActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("SELECTED_PLAN", selectedPlan);
            intent.putExtra("PLAN_PRICE", planPriceStr);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
