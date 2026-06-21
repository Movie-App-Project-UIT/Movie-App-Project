package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.pemomovie.dto.AdminSubscriptionDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpgradePremiumActivity extends AppCompatActivity {

    private LinearLayout layoutPlansContainer;
    private ProgressBar progressBar;
    private List<AdminSubscriptionDto> planList = new ArrayList<>();
    private AdminSubscriptionDto selectedPlan = null;

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

        // Nút Back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Ánh xạ
        progressBar = findViewById(R.id.progressBar);
        layoutPlansContainer = findViewById(R.id.layoutPlansContainer);

        // Nút Tiếp tục
        CardView btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                if (selectedPlan == null) {
                    Toast.makeText(UpgradePremiumActivity.this, "Vui lòng chọn một gói Premium để tiếp tục", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(UpgradePremiumActivity.this, PaymentActivity.class);
                intent.putExtra("SELECTED_PLAN_ID", selectedPlan.getId());
                intent.putExtra("SELECTED_PLAN_NAME", selectedPlan.getName());
                
                DecimalFormat df = new DecimalFormat("#,###đ");
                intent.putExtra("SELECTED_PLAN_PRICE", df.format(selectedPlan.getPrice()));
                intent.putExtra("SELECTED_PLAN_PRICE_RAW", selectedPlan.getPrice());
                intent.putExtra("SELECTED_PLAN_DURATION", selectedPlan.getDurationDays() != null ? selectedPlan.getDurationDays() : 180);
                startActivity(intent);
            });
        }

        // Tải danh sách gói Premium từ Server
        loadActivePlans();
    }

    private void loadActivePlans() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getActivePlans().enqueue(new Callback<List<AdminSubscriptionDto>>() {
            @Override
            public void onResponse(Call<List<AdminSubscriptionDto>> call, Response<List<AdminSubscriptionDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    planList.clear();
                    for (AdminSubscriptionDto p : response.body()) {
                        if (p.getIsActive()) {
                            planList.add(p);
                        }
                    }
                    renderPlans();
                } else {
                    Toast.makeText(UpgradePremiumActivity.this, "Không thể tải danh sách gói", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AdminSubscriptionDto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UpgradePremiumActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderPlans() {
        layoutPlansContainer.removeAllViews();
        if (planList.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Hiện không có gói Premium nào khả dụng.");
            emptyText.setTextColor(Color.WHITE);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setTextSize(16);
            layoutPlansContainer.addView(emptyText);
            return;
        }

        // Chọn gói đầu tiên làm mặc định nếu chưa chọn
        if (selectedPlan == null) {
            selectedPlan = planList.get(0);
        }

        DecimalFormat df = new DecimalFormat("#,###đ");

        for (int i = 0; i < planList.size(); i++) {
            final AdminSubscriptionDto plan = planList.get(i);
            View planView = getLayoutInflater().inflate(R.layout.item_upgrade_plan, layoutPlansContainer, false);

            ConstraintLayout cardPlan = (ConstraintLayout) planView;
            ImageView icCheck = planView.findViewById(R.id.icCheck);
            TextView tvBadge = planView.findViewById(R.id.tvBadge);
            TextView tvPlanName = planView.findViewById(R.id.tvPlanName);
            TextView tvPlanPrice = planView.findViewById(R.id.tvPlanPrice);
            TextView tvPlanPriceUnit = planView.findViewById(R.id.tvPlanPriceUnit);
            TextView tvPlanDescription = planView.findViewById(R.id.tvPlanDescription);

            tvPlanName.setText(plan.getName());
            tvPlanPrice.setText(df.format(plan.getPrice()));
            tvPlanDescription.setText(plan.getDescription());

            // Đơn vị giá (vd: "/ tháng" hoặc "/ 6 tháng" dựa vào duration)
            if (plan.getDurationDays() != null) {
                int days = plan.getDurationDays();
                if (days == 30) {
                    tvPlanPriceUnit.setText(" / tháng");
                } else if (days % 30 == 0) {
                    tvPlanPriceUnit.setText(" / " + (days / 30) + " tháng");
                } else {
                    tvPlanPriceUnit.setText(" / " + days + " ngày");
                }
            }

            // Gắn badge PHỔ BIẾN cho gói ở giữa nếu có từ 3 gói trở lên
            if (planList.size() >= 3 && i == planList.size() / 2) {
                tvBadge.setVisibility(View.VISIBLE);
            } else {
                tvBadge.setVisibility(View.GONE);
            }

            // Thiết lập trạng thái được chọn hay không
            int purpleColor = Color.parseColor("#8B5CF6");
            if (selectedPlan.getId().equals(plan.getId())) {
                cardPlan.setBackgroundResource(R.drawable.bg_plan_selected);
                icCheck.setImageResource(R.drawable.ic_check_circle_filled);
                icCheck.setColorFilter(purpleColor);
            } else {
                cardPlan.setBackgroundResource(R.drawable.bg_plan_unselected);
                icCheck.setImageResource(R.drawable.bg_circle_outline);
                icCheck.clearColorFilter();
            }

            // Sự kiện click
            cardPlan.setOnClickListener(v -> {
                selectedPlan = plan;
                renderPlans(); // Vẽ lại để cập nhật background/check icon
            });

            layoutPlansContainer.addView(planView);
        }
    }
}
