package com.example.pemomovie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

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

                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                startActivity(intent);
            }
        });
    }
}
