package com.example.pemomovie.ui.main;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;

public class PaymentWebActivity extends AppCompatActivity {

    private WebView webView;

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

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        webView = findViewById(R.id.webView);
        
        // Cấu hình WebView hỗ trợ thanh toán VNPay/MoMo
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Nếu là Deeplink của MoMo, mở App MoMo
                if (url != null && url.startsWith("momo://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(PaymentWebActivity.this, "Bạn chưa cài đặt ứng dụng MoMo", Toast.LENGTH_SHORT).show();
                        return true; // Vẫn chặn link này lại để WebView không bị lỗi "Webpage not available"
                    }
                }
                
                // Các link khác (VNPay, ...) mở bình thường trong WebView
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        String paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
