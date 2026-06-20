package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.AdminUserAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.AdminUserDto;
import com.example.pemomovie.utils.AdminNavigationHelper;
import com.google.android.material.chip.ChipGroup;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserActivity extends AppCompatActivity {
    private AdminUserAdapter adapter;
    private EditText edtSearch;
    private ChipGroup chipGroupFilter;
    private Boolean isPremiumFilter = null;
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutHeader), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        AdminNavigationHelper.setupBottomNavigation(this);

        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        if (rvUsers != null) {
            rvUsers.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AdminUserAdapter(this);
            rvUsers.setAdapter(adapter);
            loadUsers();
        }

        setupFilters();
    }

    private void setupFilters() {
        if (chipGroupFilter != null) {
            chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipPremium) {
                    isPremiumFilter = true;
                } else if (checkedId == R.id.chipFree) {
                    isPremiumFilter = false;
                } else {
                    isPremiumFilter = null;
                }
                loadUsers();
            });
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    currentSearch = s.toString();
                    loadUsers();
                }
            });
        }
    }

    private void loadUsers() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUsers(isPremiumFilter, currentSearch).enqueue(new Callback<List<AdminUserDto>>() {
            @Override
            public void onResponse(Call<List<AdminUserDto>> call, Response<List<AdminUserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AdminUserDto>> call, Throwable t) {
                // ignore
            }
        });
    }
}
