package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_success);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int selectedPlan = getIntent().getIntExtra("SELECTED_PLAN", 6);
        
        TextView txtPlanName = findViewById(R.id.txtPlanName);
        if (selectedPlan == 1) {
            txtPlanName.setText("Premium 1 tháng");
        } else if (selectedPlan == 12) {
            txtPlanName.setText("Premium 1 năm");
        } else {
            txtPlanName.setText("Premium 6 tháng");
        }

        TextView txtStartDate = findViewById(R.id.txtStartDate);
        TextView txtEndDate = findViewById(R.id.txtEndDate);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        txtStartDate.setText(sdf.format(startDate));

        calendar.add(Calendar.MONTH, selectedPlan);
        Date endDate = calendar.getTime();
        txtEndDate.setText(sdf.format(endDate));

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            goToHome();
        });

        findViewById(R.id.btnStartWatching).setOnClickListener(v -> {
            goToHome();
        });

        findViewById(R.id.btnGoHome).setOnClickListener(v -> {
            goToHome();
        });
    }

    private void goToHome() {
        Intent intent = new Intent(PaymentSuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
