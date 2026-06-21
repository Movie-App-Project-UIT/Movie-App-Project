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

    private Long selectedPlanId = -1L;
    private String selectedPlanName = "";
    private int selectedPlanDuration = 180;
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

        selectedPlanId = getIntent().getLongExtra("SELECTED_PLAN_ID", -1L);
        selectedPlanName = getIntent().getStringExtra("SELECTED_PLAN_NAME");
        selectedPlanDuration = getIntent().getIntExtra("SELECTED_PLAN_DURATION", 180);
        planPriceStr = getIntent().getStringExtra("PLAN_PRICE");

        // Giả lập quá trình xác nhận thanh toán mất 3 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(PaymentProcessingActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("SELECTED_PLAN_ID", selectedPlanId);
            intent.putExtra("SELECTED_PLAN_NAME", selectedPlanName);
            intent.putExtra("SELECTED_PLAN_DURATION", selectedPlanDuration);
            intent.putExtra("PLAN_PRICE", planPriceStr);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
