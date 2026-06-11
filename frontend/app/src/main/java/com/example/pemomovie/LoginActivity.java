package com.example.pemomovie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pemomovie.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);

        CheckBox cbRemember = findViewById(R.id.cbRemember);
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        boolean isRemembered = sharedPreferences.getBoolean("isRemembered", false);
        if (isRemembered) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

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
                    etEmail.setError("Email không đúng định dạng!");
                    etEmail.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    etPassword.setError("Vui lòng nhập mật khẩu");
                    etPassword.requestFocus();
                    return;
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (cbRemember.isChecked()) {
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.putBoolean("isRemembered", true);
                } else {
                    editor.clear();
                }
                editor.apply();

                Toast.makeText(LoginActivity.this, "Dữ liệu chuẩn! Đang đăng nhập...", Toast.LENGTH_SHORT).show();
                // TODO: Viết code gọi API Backend để đăng nhập ở đây
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Bạn vừa bấm Quên mật khẩu", Toast.LENGTH_SHORT).show();

                // TODO: Sau này bạn tạo xong màn hình Quên Mật Khẩu thì mở ngoặc đoạn này ra để chuyển trang
                // Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                // startActivity(intent);
            }
        });


        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị thông báo test
                Toast.makeText(LoginActivity.this, "Bạn vừa bấm Đăng ký", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
