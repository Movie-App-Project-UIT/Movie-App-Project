package com.example.pemomovie;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
                Toast.makeText(NewPasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
            }
        });
    }
}
