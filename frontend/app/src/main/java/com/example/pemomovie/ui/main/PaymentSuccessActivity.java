package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        boolean isViewPrivilege = getIntent().getBooleanExtra("IS_VIEW_PRIVILEGE", false);

        if (isViewPrivilege) {
            findViewById(R.id.stepper).setVisibility(android.view.View.GONE);
            findViewById(R.id.bottomBar).setVisibility(android.view.View.GONE);
            
            TextView tvTopBarTitle = findViewById(R.id.tvTopBarTitle);
            if (tvTopBarTitle != null) {
                tvTopBarTitle.setText("Đặc quyền");
            }

            com.example.pemomovie.api.ApiClient.getApiService().getActiveSubscription().enqueue(new retrofit2.Callback<java.util.Map<String, Object>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.Map<String, Object>> call, retrofit2.Response<java.util.Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        java.util.Map<String, Object> data = response.body();
                        String planName = (String) data.get("planName");
                        String startIso = (String) data.get("startDate");
                        String endIso = (String) data.get("endDate");

                        if (txtPlanName != null && planName != null) txtPlanName.setText(planName);

                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            if (startIso != null) {
                                Date sDate = isoFormat.parse(startIso.contains(".") ? startIso.substring(0, startIso.indexOf(".")) : startIso);
                                if (txtStartDate != null && sDate != null) txtStartDate.setText(displayFormat.format(sDate));
                            }
                            if (endIso != null) {
                                Date eDate = isoFormat.parse(endIso.contains(".") ? endIso.substring(0, endIso.indexOf(".")) : endIso);
                                if (txtEndDate != null && eDate != null) txtEndDate.setText(displayFormat.format(eDate));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        View layoutPackageInfo = findViewById(R.id.layoutPackageInfo);
                        if (layoutPackageInfo != null) layoutPackageInfo.setVisibility(android.view.View.GONE);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<java.util.Map<String, Object>> call, Throwable t) {
                    View layoutPackageInfo = findViewById(R.id.layoutPackageInfo);
                    if (layoutPackageInfo != null) layoutPackageInfo.setVisibility(android.view.View.GONE);
                }
            });
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isViewPrivilege) {
                finish();
            } else {
                goToHome();
            }
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
