package com.example.pemomovie.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.dto.SyncUserRequest;
import com.example.pemomovie.dto.UserProfileDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        TextView tvLogin = findViewById(R.id.tvLogin);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignup = findViewById(R.id.btnSignup);

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (username.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên hiển thị");
                etUsername.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                etEmail.setError("Vui lòng nhập Email");
                etEmail.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
                etPassword.requestFocus();
                return;
            }

            if (!confirmPassword.equals(password)) {
                etConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
                etConfirmPassword.requestFocus();
                return;
            }

            Toast.makeText(SignUpActivity.this, "Đang xử lý...", Toast.LENGTH_SHORT).show();

            Log.d("SignUpActivity", "Bắt đầu đăng ký Firebase cho email: " + email);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("SignUpActivity", "Đăng ký Firebase thành công! UID: " + user.getUid());

                            // Cập nhật Tên hiển thị lên hệ thống Firebase
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                user.updateProfile(profileUpdates);
                            }

                            // Gọi hàm đồng bộ thông tin tài khoản xuống MySQL Database ngay lập tức
                            syncUserWithBackend(email, username, "");
                        }
                        else {
                            Exception exception = task.getException();
                            String errMsg = exception != null ? exception.getMessage() : "Lỗi không xác định";
                            Log.e("SignUpActivity", "Đăng ký thất bại: " + errMsg, exception);
                            
                            String vietnameseErrMsg = errMsg;
                            if (errMsg.toLowerCase().contains("already in use") || errMsg.toLowerCase().contains("collision")) {
                                vietnameseErrMsg = "Địa chỉ email này đã được đăng ký bởi một tài khoản khác!";
                            } else if (errMsg.toLowerCase().contains("badly formatted")) {
                                vietnameseErrMsg = "Định dạng email không hợp lệ!";
                            } else if (errMsg.toLowerCase().contains("weak password")) {
                                vietnameseErrMsg = "Mật khẩu quá yếu! Hãy sử dụng ít nhất 6 ký tự.";
                            } else if (errMsg.toLowerCase().contains("network") || errMsg.toLowerCase().contains("timeout")) {
                                vietnameseErrMsg = "Kết nối mạng thất bại. Vui lòng kiểm tra lại đường truyền internet.";
                            } else {
                                vietnameseErrMsg = "Đã xảy ra lỗi đăng ký, vui lòng thử lại sau.";
                            }
                            
                            showErrorDialog(vietnameseErrMsg);
                        }
                    });
        });
    }

    private void showSuccessDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thành công")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lỗi đăng ký")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void syncUserWithBackend(String email, String username, String avatarUrl) {
        SyncUserRequest request = new SyncUserRequest(email, username, avatarUrl);
        ApiClient.getApiService().syncUser(request).enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful()) {
                    showSuccessDialog("Đăng ký tài khoản thành công!");
                } else {
                    showErrorDialog("Đồng bộ dữ liệu máy chủ thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                showErrorDialog("Lỗi kết nối máy chủ: " + t.getMessage());
            }
        });
    }

}
