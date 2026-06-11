package com.example.pemomovie;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyCodeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);
        EditText etOtp1 = findViewById(R.id.etOtp1);
        EditText etOtp2 = findViewById(R.id.etOtp2);
        EditText etOtp3 = findViewById(R.id.etOtp3);
        EditText etOtp4 = findViewById(R.id.etOtp4);
        Button btnVerify = findViewById(R.id.btnVerify);
        TextView tvResend = findViewById(R.id.tvResend);

        setupOtpInput(etOtp1, etOtp2);
        setupOtpInput(etOtp2, etOtp3);
        setupOtpInput(etOtp3, etOtp4);
        setupOtpInput(etOtp4, null);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = etOtp1.getText().toString() + etOtp2.getText().toString() +
                        etOtp3.getText().toString() + etOtp4.getText().toString();
                if (otp.length() < 4) {
                    Toast.makeText(VerifyCodeActivity.this, "Vui lòng nhập đủ 4 số mã xác thực", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(VerifyCodeActivity.this, NewPasswordActivity.class);
                startActivity(intent);
            }
        });
        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VerifyCodeActivity.this, "Đã gửi lại mã xác thực vào email của bạn!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupOtpInput(EditText currentEt, EditText nextEt) {
        currentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextEt != null) {
                    nextEt.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
