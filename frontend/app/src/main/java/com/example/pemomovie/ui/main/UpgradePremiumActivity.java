package com.example.pemomovie.ui.main;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

public class UpgradePremiumActivity extends AppCompatActivity {

    private ConstraintLayout cardPlan1Month, cardPlan6Month, cardPlan1Year;
    private ImageView icCheck1, icCheck6, icCheck12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upgrade_premium);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gạch ngang giá cũ
        TextView txtOldPrice6 = findViewById(R.id.txtOldPrice6);
        if (txtOldPrice6 != null) {
            txtOldPrice6.setPaintFlags(txtOldPrice6.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        TextView txtOldPrice12 = findViewById(R.id.txtOldPrice12);
        if (txtOldPrice12 != null) {
            txtOldPrice12.setPaintFlags(txtOldPrice12.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Nút Back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ánh xạ các plan
        cardPlan1Month = findViewById(R.id.cardPlan1Month);
        cardPlan6Month = findViewById(R.id.cardPlan6Month);
        cardPlan1Year = findViewById(R.id.cardPlan1Year);

        icCheck1 = findViewById(R.id.icCheck1);
        icCheck6 = findViewById(R.id.icCheck6);
        icCheck12 = findViewById(R.id.icCheck12);

        // Sự kiện click chọn gói
        if (cardPlan1Month != null) cardPlan1Month.setOnClickListener(v -> selectPlan(1));
        if (cardPlan6Month != null) cardPlan6Month.setOnClickListener(v -> selectPlan(6));
        if (cardPlan1Year != null) cardPlan1Year.setOnClickListener(v -> selectPlan(12));

        // Nút Tiếp tục
        CardView btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                Toast.makeText(this, "Chuyển sang trang thanh toán...", Toast.LENGTH_SHORT).show();
                // Thực hiện logic chuyển sang Activity thanh toán ở đây
            });
        }
    }

    private void selectPlan(int planType) {
        // Reset tất cả về unselected
        cardPlan1Month.setBackgroundResource(R.drawable.bg_plan_unselected);
        cardPlan6Month.setBackgroundResource(R.drawable.bg_plan_unselected);
        cardPlan1Year.setBackgroundResource(R.drawable.bg_plan_unselected);

        icCheck1.setImageResource(R.drawable.bg_circle_outline);
        icCheck1.clearColorFilter();
        icCheck6.setImageResource(R.drawable.bg_circle_outline);
        icCheck6.clearColorFilter();
        icCheck12.setImageResource(R.drawable.bg_circle_outline);
        icCheck12.clearColorFilter();

        int purpleColor = android.graphics.Color.parseColor("#8B5CF6");

        // Set state cho plan được chọn
        if (planType == 1) {
            cardPlan1Month.setBackgroundResource(R.drawable.bg_plan_selected);
            icCheck1.setImageResource(R.drawable.ic_check_circle_filled);
            icCheck1.setColorFilter(purpleColor);
        } else if (planType == 6) {
            cardPlan6Month.setBackgroundResource(R.drawable.bg_plan_selected);
            icCheck6.setImageResource(R.drawable.ic_check_circle_filled);
            icCheck6.setColorFilter(purpleColor);
        } else if (planType == 12) {
            cardPlan1Year.setBackgroundResource(R.drawable.bg_plan_selected);
            icCheck12.setImageResource(R.drawable.ic_check_circle_filled);
            icCheck12.setColorFilter(purpleColor);
        }
    }
}
