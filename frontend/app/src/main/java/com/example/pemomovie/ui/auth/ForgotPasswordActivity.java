package com.example.pemomovie.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        EditText etEmail = findViewById(R.id.etEmail);
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    etEmail.setError("Vui lòng nhập Email");
                    etEmail.requestFocus();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không hợp lệ!");
                    etEmail.requestFocus();
                    return;
                }

                Toast.makeText(ForgotPasswordActivity.this, "Đang gửi email khôi phục...", Toast.LENGTH_SHORT).show();
                btnNext.setEnabled(false); // Chống click đúp gây ra nhiều email làm hết hạn link

                com.example.pemomovie.api.ApiClient.getApiService().forgotPassword(new com.example.pemomovie.dto.EmailRequest(email))
                        .enqueue(new retrofit2.Callback<com.example.pemomovie.dto.MessageResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.pemomovie.dto.MessageResponse> call, retrofit2.Response<com.example.pemomovie.dto.MessageResponse> response) {
                                btnNext.setEnabled(true);
                                if (response.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi mã xác nhận. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                                    android.content.Intent intent = new android.content.Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi: Email không tồn tại", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.example.pemomovie.dto.MessageResponse> call, Throwable t) {
                                btnNext.setEnabled(true);
                                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
