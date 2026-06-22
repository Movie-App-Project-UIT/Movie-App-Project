package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

import java.util.HashMap;
import java.util.Map;

public class PaymentWebActivity extends AppCompatActivity {

    private WebView webView;
    private String selectedPlanName;
    private int selectedPlanDuration;
    private String planPriceStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy thông tin gói để truyền sang PaymentSuccessActivity
        selectedPlanName = getIntent().getStringExtra("SELECTED_PLAN_NAME");
        selectedPlanDuration = getIntent().getIntExtra("SELECTED_PLAN_DURATION", 180);
        planPriceStr = getIntent().getStringExtra("PLAN_PRICE");

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        webView = findViewById(R.id.webView);

        // Cấu hình WebView hỗ trợ thanh toán VNPay
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            private String lastInterceptedUrl = "";

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // ✅ Bắt Deep Link từ Backend sau khi thanh toán VNPay xong
                if (url != null && url.startsWith("pemomovie://payment")) {
                    Uri uri = Uri.parse(url);
                    String status = uri.getQueryParameter("status");
                    if ("success".equals(status)) {
                        // Thanh toán thành công → mở màn hình thành công
                        Intent intent = new Intent(PaymentWebActivity.this, PaymentSuccessActivity.class);
                        intent.putExtra("SELECTED_PLAN_NAME", selectedPlanName);
                        intent.putExtra("SELECTED_PLAN_DURATION", selectedPlanDuration);
                        intent.putExtra("PLAN_PRICE", planPriceStr);
                        startActivity(intent);
                    } else {
                        // Thanh toán thất bại
                        Toast.makeText(PaymentWebActivity.this,
                                "Thanh toán thất bại hoặc đã bị hủy. Vui lòng thử lại!",
                                Toast.LENGTH_LONG).show();
                    }
                    finish();
                    return true;
                }

                // Thêm header bypass ngrok warning khi load các URL ngrok
                if (url != null && url.contains("ngrok-free.dev") && !url.equals(lastInterceptedUrl)) {
                    lastInterceptedUrl = url;
                    view.post(() -> {
                        Map<String, String> extraHeaders = new HashMap<>();
                        extraHeaders.put("ngrok-skip-browser-warning", "true");
                        view.loadUrl(url, extraHeaders);
                    });
                    return true;
                } else if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    return false; // WebView tự xử lý
                } else {
                    // Mở ứng dụng ngoài (VNPay, ngân hàng,...)
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(PaymentWebActivity.this, "Không tìm thấy ứng dụng hỗ trợ", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        String paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            Map<String, String> extraHeaders = new HashMap<>();
            extraHeaders.put("ngrok-skip-browser-warning", "true");
            webView.loadUrl(paymentUrl, extraHeaders);
        } else {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
