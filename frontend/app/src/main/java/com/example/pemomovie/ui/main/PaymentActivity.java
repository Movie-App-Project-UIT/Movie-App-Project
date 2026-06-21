package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.dto.UserProfileDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private ConstraintLayout cardPayMomo, cardPayVnpay;
    private ImageView icCheckMomo, icCheckVnpay;
    private String selectedMethod = "MOMO"; // Default
    private int selectedPlan = 6;
    private String planPriceStr = "249.000đ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy gói đã chọn
        selectedPlan = getIntent().getIntExtra("SELECTED_PLAN", 6);
        
        TextView txtPlanName = findViewById(R.id.txtPlanName);
        TextView txtPlanPrice = findViewById(R.id.txtPlanPrice);
        TextView txtPayBtn = findViewById(R.id.txtPayBtn);
        
        if (selectedPlan == 1) {
            txtPlanName.setText("Gói 1 tháng");
            planPriceStr = "49.000đ";
        } else if (selectedPlan == 12) {
            txtPlanName.setText("Gói 1 năm");
            planPriceStr = "449.000đ";
        } else {
            txtPlanName.setText("Gói 6 tháng");
            planPriceStr = "249.000đ";
        }
        
        txtPlanPrice.setText(planPriceStr);
        // Bỏ dòng cập nhật text nút bấm để luôn giữ là "Tiếp tục thanh toán"
        // txtPayBtn.setText("Thanh toán " + planPriceStr);

        // Nút Back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ánh xạ payment methods
        cardPayMomo = findViewById(R.id.cardPayMomo);
        cardPayVnpay = findViewById(R.id.cardPayVnpay);

        icCheckMomo = findViewById(R.id.icCheckMomo);
        icCheckVnpay = findViewById(R.id.icCheckVnpay);

        if (cardPayMomo != null) cardPayMomo.setOnClickListener(v -> selectMethod("MOMO"));
        if (cardPayVnpay != null) cardPayVnpay.setOnClickListener(v -> selectMethod("VNPAY"));

        // Lấy thông tin user hiện tại (email)
        loadUserEmail();

        // Nút Thanh toán
        CardView btnPay = findViewById(R.id.btnPay);
        if (btnPay != null) {
            btnPay.setOnClickListener(v -> {
                Intent intent = new Intent(PaymentActivity.this, QrPaymentActivity.class);
                intent.putExtra("SELECTED_PLAN", selectedPlan);
                intent.putExtra("PLAN_PRICE", planPriceStr);
                intent.putExtra("PAYMENT_METHOD", selectedMethod);
                startActivity(intent);
            });
        }
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        
        // Reset tất cả
        cardPayMomo.setBackgroundResource(R.drawable.bg_plan_unselected);
        cardPayVnpay.setBackgroundResource(R.drawable.bg_plan_unselected);
        
        icCheckMomo.setImageResource(R.drawable.bg_circle_outline);
        icCheckMomo.clearColorFilter();
        icCheckVnpay.setImageResource(R.drawable.bg_circle_outline);
        icCheckVnpay.clearColorFilter();

        int purpleColor = android.graphics.Color.parseColor("#8B5CF6");

        // Set state cho method được chọn
        if (method.equals("MOMO")) {
            cardPayMomo.setBackgroundResource(R.drawable.bg_plan_selected);
            icCheckMomo.setImageResource(R.drawable.ic_check_circle_filled);
            icCheckMomo.setColorFilter(purpleColor);
        } else if (method.equals("VNPAY")) {
            cardPayVnpay.setBackgroundResource(R.drawable.bg_plan_selected);
            icCheckVnpay.setImageResource(R.drawable.ic_check_circle_filled);
            icCheckVnpay.setColorFilter(purpleColor);
        }
    }

    private void loadUserEmail() {
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TextView txtUserEmail = findViewById(R.id.txtUserEmail);
                    if (txtUserEmail != null) {
                        String email = response.body().getEmail();
                        if (email != null && !email.isEmpty()) {
                            txtUserEmail.setText(email);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                // Ignore
            }
        });
    }
}
