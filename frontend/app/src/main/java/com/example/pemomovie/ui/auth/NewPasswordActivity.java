package com.example.pemomovie.ui.auth;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;

public class NewPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnChange = findViewById(R.id.btnChange);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                // Validate
                if (password.length() < 6) {
                    etNewPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
                    etNewPassword.requestFocus();
                    return;
                }
                if (!confirmPassword.equals(password)) {
                    etConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
                    etConfirmPassword.requestFocus();
                    return;
                }

                String email = getIntent().getStringExtra("email");
                String code = getIntent().getStringExtra("code");
                if (email == null || code == null) {
                    Toast.makeText(NewPasswordActivity.this, "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnChange.setEnabled(false);
                com.example.pemomovie.api.ApiClient.getApiService().resetPassword(new com.example.pemomovie.dto.ResetPasswordRequest(email, code, password))
                        .enqueue(new retrofit2.Callback<com.example.pemomovie.dto.MessageResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.pemomovie.dto.MessageResponse> call, retrofit2.Response<com.example.pemomovie.dto.MessageResponse> response) {
                                btnChange.setEnabled(true);
                                if (response.isSuccessful()) {
                                    Toast.makeText(NewPasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(NewPasswordActivity.this, "Đổi mật khẩu thất bại, mã có thể đã hết hạn!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.example.pemomovie.dto.MessageResponse> call, Throwable t) {
                                btnChange.setEnabled(true);
                                Toast.makeText(NewPasswordActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
