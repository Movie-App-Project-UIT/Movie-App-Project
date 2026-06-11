package com.example.pemomovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends  AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView tvLogin = findViewById(R.id.tvLogin);
        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignup = findViewById(R.id.btnSignup);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (username.isEmpty()) {
                    etUsername.setError("Vui lòng nhập tên đăng nhập");
                    etUsername.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    etEmail.setError("Vui lòng nhập Email");
                    etEmail.requestFocus();
                    return;
                }

                if (!email.endsWith("@gmail.com")) {
                    etEmail.setError("Chỉ chấp nhận tài khoản @gmail.com!");
                    etEmail.requestFocus();
                    return;
                }

                if (email.equals("@gmail.com") || email.length() <= 10) {
                    etEmail.setError("Tên email không hợp lệ!");
                    etEmail.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không hợp lệ!");
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
                Toast.makeText(SignUpActivity.this, "Bạn vừa ấn nút đăng ký!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
