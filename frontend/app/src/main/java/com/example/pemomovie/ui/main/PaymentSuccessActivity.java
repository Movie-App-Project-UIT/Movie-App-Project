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

        String selectedPlanName = getIntent().getStringExtra("SELECTED_PLAN_NAME");
        if (selectedPlanName == null) selectedPlanName = "Premium";
        int selectedPlanDuration = getIntent().getIntExtra("SELECTED_PLAN_DURATION", 180);
        
        TextView txtPlanName = findViewById(R.id.txtPlanName);
        if (txtPlanName != null) {
            txtPlanName.setText(selectedPlanName);
        }

        TextView txtStartDate = findViewById(R.id.txtStartDate);
        TextView txtEndDate = findViewById(R.id.txtEndDate);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        if (txtStartDate != null) {
            txtStartDate.setText(sdf.format(startDate));
        }

        calendar.add(Calendar.DAY_OF_YEAR, selectedPlanDuration);
        Date endDate = calendar.getTime();
        if (txtEndDate != null) {
            txtEndDate.setText(sdf.format(endDate));
        }

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
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean("has_new_premium_notification", true).apply();

        Intent intent = new Intent(PaymentSuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
