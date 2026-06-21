package com.example.pemomovie.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pemomovie.R;
import com.example.pemomovie.ui.auth.LoginActivity;
import com.example.pemomovie.utils.AdminNavigationHelper;

public class AdminProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile);
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        
        AdminNavigationHelper.setupBottomNavigation(this);

        loadAdminData();

        View btnDashboard = findViewById(R.id.btnDashboard);
        if (btnDashboard != null) {
            btnDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.pemomovie.ui.main.AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }
        
        View btnManageUsers = findViewById(R.id.btnManageUsers);
        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    
    private void loadAdminData() {
        android.widget.TextView txtUserName = findViewById(R.id.txtUserName);
        android.widget.TextView txtUserEmail = findViewById(R.id.txtUserEmail);
        android.widget.ImageView imgAvatar = findViewById(R.id.imgAvatar);
        
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                txtUserName.setText(currentUser.getDisplayName());
            }
            if (currentUser.getEmail() != null) {
                txtUserEmail.setText(currentUser.getEmail());
            }
            if (currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                com.bumptech.glide.Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .into(imgAvatar);
            }
        }
    }
}
