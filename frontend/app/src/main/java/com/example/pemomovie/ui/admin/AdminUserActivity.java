package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminUserAdapter;
import com.example.pemomovie.mockdata.MockDataHelper;
import com.example.pemomovie.utils.AdminNavigationHelper;

public class AdminUserActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);
        AdminNavigationHelper.setupBottomNavigation(this);

        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        if (rvUsers != null) {
            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            AdminUserAdapter adapter = new AdminUserAdapter(this);
            adapter.setUsers(MockDataHelper.getMockUsers());
            rvUsers.setAdapter(adapter);
        }
    }
}
