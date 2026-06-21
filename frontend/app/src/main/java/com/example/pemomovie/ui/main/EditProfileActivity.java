package com.example.pemomovie.ui.main;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.dto.SyncUserRequest;
import com.example.pemomovie.dto.UserProfileDto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextView txtDisplayName, txtEmail;
    private ImageView imgAvatar;

    // Lưu tạm dữ liệu để đồng bộ API
    private String currentEmail = "";
    private String currentAvatarUrl = "";
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        View rootView = ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        txtDisplayName = findViewById(R.id.txtDisplayName);
        txtEmail = findViewById(R.id.txtEmail);
        imgAvatar = findViewById(R.id.imgAvatar);

        ConstraintLayout btnEditDisplayName = findViewById(R.id.btnEditDisplayName);
        ConstraintLayout btnChangePassword = findViewById(R.id.btnChangePassword);

        if (btnEditDisplayName != null) {
            btnEditDisplayName.setOnClickListener(v -> showEditNameDialog());
        }
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showEditPasswordDialog());
        }

        // Bắt sự kiện nút Lưu thay đổi ở đáy màn hình
        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveChangesToDatabase());
        }

        // Xử lý nút Back quay lại
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatarToBackend(uri);
                    }
                });

        ImageView btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> {
                pickImageLauncher.launch("image/*");
            });
        }

        // Bắt đầu tải dữ liệu người dùng
        loadUserProfile();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void saveChangesToDatabase() {
        String newName = txtDisplayName != null ? txtDisplayName.getText().toString() : "";
        if (newName.isEmpty() || newName.equals("User")) {
            Toast.makeText(this, "Vui lòng cập nhật tên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        SyncUserRequest request = new SyncUserRequest(currentEmail, newName, currentAvatarUrl);
        ApiClient.getApiService().syncUser(request).enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful()) {
                    // Cập nhật lại Tên hiển thị trên Firebase để đồng bộ với Dropdown
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName);
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            profileUpdatesBuilder.setPhotoUri(Uri.parse(currentAvatarUrl));
                        }
                        UserProfileChangeRequest profileUpdates = profileUpdatesBuilder.build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                            Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ", Toast.LENGTH_SHORT).show();
                            finish(); // Trở về màn hình trước (ProfileActivity)
                        });
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Đã lưu thay đổi hồ sơ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không thể lưu hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile() {
        ApiClient.getApiService().getMyProfile().enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileDto profile = response.body();

                    if (txtDisplayName != null) {
                        String name = profile.getName();
                        if (name == null || name.trim().isEmpty()) {
                            txtDisplayName.setText("User");
                        } else {
                            txtDisplayName.setText(name);
                        }
                    }

                    if (txtEmail != null && profile.getEmail() != null) {
                        currentEmail = profile.getEmail();
                        txtEmail.setText(profile.getEmail());
                    }

                    if (imgAvatar != null) {
                        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().trim().isEmpty()) {
                            String avatarUrl = profile.getAvatarUrl().trim();
                            if (avatarUrl.startsWith("\"") && avatarUrl.endsWith("\"")) {
                                avatarUrl = avatarUrl.substring(1, avatarUrl.length() - 1);
                            }
                            currentAvatarUrl = avatarUrl;
                            int paddingPx = (int) (3 * getResources().getDisplayMetrics().density);
                            imgAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                            imgAvatar.setBackgroundColor(Color.parseColor("#8C8E92"));
                            Glide.with(EditProfileActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_avatar)
                                    .circleCrop()
                                    .into(imgAvatar);
                        } else {
                            imgAvatar.setBackgroundColor(Color.parseColor("#A7F3D0"));
                            imgAvatar.setPadding(0, 0, 0, 0);
                            imgAvatar.setImageResource(R.drawable.ic_avatar);
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                        android.util.Log.e("EditProfile", "Lỗi tải hồ sơ: Mã " + response.code() + " - " + errorBody);
                    } catch (Exception e) {}
                    Toast.makeText(EditProfileActivity.this, "Không thể tải hồ sơ (Lỗi " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 1. Hiển thị Bottom Sheet đổi Tên hiển thị
    private void showEditNameDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_edit_name, null);
        dialog.setContentView(view);

        // Khắc phục lỗi lọt màu nền chính ở đáy (thanh điều hướng)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setNavigationBarColor(Color.parseColor("#0F1221"));
        }

        EditText edtDisplayName = view.findViewById(R.id.edtDisplayName);
        if (txtDisplayName != null) {
            edtDisplayName.setText(txtDisplayName.getText().toString());
        }

        findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        findViewById(R.id.btnSubmitName).setOnClickListener(v -> {
            String newName = edtDisplayName.getText().toString().trim();
            if (!newName.isEmpty()) {
                if (txtDisplayName != null) {
                    txtDisplayName.setText(newName);
                }
                Toast.makeText(this, "Đã lưu tên hiển thị", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Vui lòng nhập tên hiển thị", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // 2. Hiển thị Bottom Sheet đổi Mật khẩu
    private void showEditPasswordDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_edit_password, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setNavigationBarColor(Color.parseColor("#0F1221"));
        }

        EditText edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);
        EditText edtNewPassword = view.findViewById(R.id.edtNewPassword);
        EditText edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSubmitPassword).setOnClickListener(v -> {
            String currentPw = edtCurrentPassword.getText().toString();
            String newPw = edtNewPassword.getText().toString();
            String confirmPw = edtConfirmPassword.getText().toString();

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPw.equals(confirmPw)) {
                Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi Firebase Auth để đổi mật khẩu
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                // 1. Xác thực lại (Re-authenticate) bằng mật khẩu hiện tại
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPw);

                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2. Xác thực thành công, tiến hành đổi mật khẩu mới
                        user.updatePassword(newPw).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(this, "Đã cập nhật mật khẩu mới thành công", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(this, "Lỗi khi cập nhật mật khẩu: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Xác thực thất bại (mật khẩu hiện tại sai)
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Lỗi xác thực: Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void uploadAvatarToBackend(Uri imageUri) {
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File tempFile = new File(getCacheDir(), "avatar_temp.jpg");
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            ApiClient.getApiService().uploadAvatar(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String newUrl = response.body().string();
                            if (newUrl != null) {
                                newUrl = newUrl.trim();
                                if (newUrl.startsWith("\"") && newUrl.endsWith("\"")) {
                                    newUrl = newUrl.substring(1, newUrl.length() - 1);
                                }
                            }
                            currentAvatarUrl = newUrl;
                            if (imgAvatar != null) {
                                int paddingPx = (int) (3 * getResources().getDisplayMetrics().density);
                                imgAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                                imgAvatar.setBackgroundColor(Color.parseColor("#8C8E92"));
                                Glide.with(EditProfileActivity.this)
                                        .load(newUrl)
                                        .placeholder(R.drawable.ic_avatar)
                                        .circleCrop()
                                        .into(imgAvatar);
                            }
                            Toast.makeText(EditProfileActivity.this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Lỗi đọc URL ảnh", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            String errorStr = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                            android.util.Log.e("UploadAvatar", "Lỗi Backend: Mã " + response.code() + " - " + errorStr);
                            Toast.makeText(EditProfileActivity.this, "Lỗi " + response.code() + ": Xem Logcat", Toast.LENGTH_LONG).show();
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(EditProfileActivity.this, "Lỗi mạng khi tải ảnh", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
