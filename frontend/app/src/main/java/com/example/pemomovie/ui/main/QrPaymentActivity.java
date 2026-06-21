package com.example.pemomovie.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pemomovie.R;
import com.example.pemomovie.api.ApiClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrPaymentActivity extends AppCompatActivity {

    private int selectedPlan = 6;
    private String planPriceStr = "249.000đ";
    private String paymentMethod = "MOMO";
    private CountDownTimer countDownTimer;
    private TextView txtTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qr_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedPlan = getIntent().getIntExtra("SELECTED_PLAN", 6);
        planPriceStr = getIntent().getStringExtra("PLAN_PRICE");
        if (planPriceStr == null) planPriceStr = "249.000đ";
        paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");
        if (paymentMethod == null) paymentMethod = "MOMO";

        // Cập nhật UI theo phương thức thanh toán
        TextView txtAppInstruction = findViewById(R.id.txtAppInstruction);
        TextView txtSecurityInfo = findViewById(R.id.txtSecurityInfo);
        ImageView imgQrCenterLogo = findViewById(R.id.imgQrCenterLogo);
        ImageView imgQrCode = findViewById(R.id.imgQrCode);
        
        if ("MOMO".equals(paymentMethod)) {
            txtAppInstruction.setText("Sử dụng ứng dụng MoMo để quét mã");
            txtSecurityInfo.setText("Giao dịch được bảo mật bởi MoMo");
            if (imgQrCenterLogo != null) imgQrCenterLogo.setImageResource(R.drawable.logo_momo);
        } else if ("VNPAY".equals(paymentMethod)) {
            txtAppInstruction.setText("Sử dụng ứng dụng ngân hàng hoặc VNPay để quét mã");
            txtSecurityInfo.setText("Giao dịch được bảo mật bởi VNPay");
            if (imgQrCenterLogo != null) imgQrCenterLogo.setImageResource(R.drawable.logo_vnpay);
        } else {
            txtAppInstruction.setText("Quét mã QR bằng ứng dụng ngân hàng");
            txtSecurityInfo.setText("Giao dịch được bảo mật an toàn");
        }

        TextView txtPlanPrice = findViewById(R.id.txtPlanPrice);
        txtPlanPrice.setText(planPriceStr);

        TextView txtOrderId = findViewById(R.id.txtOrderId);
        long randomId = (long) (Math.random() * 900000) + 100000;
        String orderId = "PREMIUM" + selectedPlan + "T-" + randomId;
        txtOrderId.setText("Nội dung: " + orderId);

        // Chuyển giá tiền (vd: "249.000đ") thành số nguyên ("249000")
        String numericPrice = planPriceStr.replaceAll("[^0-9]", "");
        long finalAmount = 99000L;
        try {
            finalAmount = Long.parseLong(numericPrice);
        } catch (Exception e) {}

        // Gọi API thực tế từ Backend để lấy URL thanh toán (Sandbox MoMo / VNPay)
        Toast.makeText(this, "Đang lấy mã " + paymentMethod + " từ Server...", Toast.LENGTH_SHORT).show();
        ApiClient.getApiService().createPaymentUrl(2L, paymentMethod, finalAmount) // Truyền ID gói và giá tiền động
            .enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String paymentUrl = response.body().string();
                            // Render URL này thành mã QR để quét
                            runOnUiThread(() -> generateQrCode(paymentUrl, imgQrCode));
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(QrPaymentActivity.this, "Lỗi đọc dữ liệu QR", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(QrPaymentActivity.this, "Lỗi tạo mã QR từ Server", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    runOnUiThread(() -> Toast.makeText(QrPaymentActivity.this, "Không thể kết nối Server", Toast.LENGTH_SHORT).show());
                }
            });

        // Nút Back
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Nút làm mới QR
        LinearLayout btnRefreshQr = findViewById(R.id.btnRefreshQr);
        if (btnRefreshQr != null) {
            btnRefreshQr.setOnClickListener(v -> {
                Toast.makeText(this, "Đã làm mới mã QR", Toast.LENGTH_SHORT).show();
                startTimer();
            });
        }

        // Nút hoàn tất
        CardView btnPayComplete = findViewById(R.id.btnPayComplete);
        if (btnPayComplete != null) {
            btnPayComplete.setOnClickListener(v -> {
                Toast.makeText(this, "Vui lòng sử dụng 2 nút Test bên dưới để giả lập luồng thanh toán nhé!", Toast.LENGTH_LONG).show();
            });
        }

        // Test Buttons
        androidx.cardview.widget.CardView btnTestSuccess = findViewById(R.id.btnTestSuccess);
        if (btnTestSuccess != null) {
            btnTestSuccess.setOnClickListener(v -> {
                Toast.makeText(this, "Đang gửi tín hiệu giả lập thanh toán lên Server...", Toast.LENGTH_SHORT).show();
                ApiClient.getApiService().simulateSuccess(2L) // 2L là ID của gói Premium mặc định
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                // Gọi Server thành công, chuyển sang màn hình loading
                                Intent intent = new Intent(QrPaymentActivity.this, PaymentProcessingActivity.class);
                                intent.putExtra("SELECTED_PLAN", selectedPlan);
                                intent.putExtra("PLAN_PRICE", planPriceStr);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(QrPaymentActivity.this, "Lỗi Server khi nâng cấp VIP", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(QrPaymentActivity.this, "Không thể kết nối Backend", Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        }

        androidx.cardview.widget.CardView btnTestFailure = findViewById(R.id.btnTestFailure);
        if (btnTestFailure != null) {
            btnTestFailure.setOnClickListener(v -> {
                Toast.makeText(QrPaymentActivity.this, "Giao dịch thất bại hoặc bị hủy. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        txtTimer = findViewById(R.id.txtTimer);
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // 15 phút
        long duration = 15 * 60 * 1000;
        
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                if (txtTimer != null) {
                    txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (txtTimer != null) {
                    txtTimer.setText("00:00");
                }
            }
        }.start();
    }

    private void generateQrCode(String data, ImageView imgQrCode) {
        if (imgQrCode == null) return;
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            // Bật chế độ sửa lỗi mức H (High - 30%) để dù logo ở giữa che khuất thì vẫn quét được
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // Giảm viền trắng xung quanh QR

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512, hints);
            
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imgQrCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
