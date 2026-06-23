package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminUserDto;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private List<AdminUserDto> users = new ArrayList<>();
    private Context context;

    public AdminUserAdapter(Context context) {
        this.context = context;
    }

    public void setUsers(List<AdminUserDto> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUserDto user = users.get(position);
        holder.txtUserName.setText(user.getUsername() != null ? user.getUsername() : "No name");
        holder.txtUserEmail.setText(user.getEmail());

        if (holder.imgAvatar != null) {
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null) {
                avatarUrl = avatarUrl.trim();
                if (avatarUrl.startsWith("\"") && avatarUrl.endsWith("\"")) {
                    avatarUrl = avatarUrl.substring(1, avatarUrl.length() - 1);
                }
                avatarUrl = avatarUrl.trim();
                if ("null".equalsIgnoreCase(avatarUrl) || "undefined".equalsIgnoreCase(avatarUrl)) {
                    avatarUrl = null;
                }
            }
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                if (avatarUrl.startsWith("/")) {
                    String baseUrl = com.example.pemomovie.BuildConfig.BASE_URL;
                    if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    avatarUrl = baseUrl + avatarUrl;
                }
                holder.imgAvatar.setImageTintList(null);
                holder.imgAvatar.setColorFilter(null);
                holder.imgAvatar.clearColorFilter();
                holder.imgAvatar.setPadding(0, 0, 0, 0);
                holder.imgAvatar.setBackgroundResource(R.drawable.bg_circle_button);
                holder.imgAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#374151")));
                com.bumptech.glide.Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .error(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.ic_avatar);
                holder.imgAvatar.setImageTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(context, R.color.gray_text_light)));
                holder.imgAvatar.setBackgroundResource(R.drawable.bg_circle_button);
                holder.imgAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#374151")));
                float density = context.getResources().getDisplayMetrics().density;
                int paddingPx = (int) (8 * density + 0.5f);
                holder.imgAvatar.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            }
        }

        if ("PREMIUM".equals(user.getTier())) {
            holder.txtTier.setText("PREMIUM");
            holder.txtTier.setTextColor(Color.parseColor("#F59E0B")); // Gold
            holder.txtTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4DF59E0B"))); // Translucent Gold
        } else {
            holder.txtTier.setText("FREE");
            holder.txtTier.setTextColor(Color.parseColor("#9CA3AF")); // Gray
            holder.txtTier.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4D9CA3AF"))); // Translucent Gray
        }
        
        if (user.isActive()) {
            holder.txtStatus.setText("Hoạt động");
            holder.txtStatus.setTextColor(Color.parseColor("#10B981")); // Green
            holder.txtStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3310B981")));
            
            holder.btnAction.setImageResource(R.drawable.ic_delete);
            holder.btnAction.setColorFilter(Color.parseColor("#EF4444"));
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33EF4444")));
        } else {
            holder.txtStatus.setText("Vô hiệu hóa");
            holder.txtStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.txtStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#33EF4444")));
            
            holder.btnAction.setImageResource(R.drawable.ic_restore); // Cần đảm bảo ic_restore tồn tại
            holder.btnAction.setColorFilter(Color.parseColor("#10B981"));
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3310B981")));
        }

        holder.btnAction.setOnClickListener(v -> showConfirmDialog(user, position));
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.example.pemomovie.ui.admin.AdminUserDetailActivity.class);
            intent.putExtra("USER_ID", user.getId());
            context.startActivity(intent);
        });
    }

    private void showConfirmDialog(AdminUserDto user, int position) {
        String action = user.isActive() ? "vô hiệu hóa" : "kích hoạt";
        
        android.app.Dialog dialog = new android.app.Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        android.widget.ImageView icon = dialog.findViewById(R.id.dialogIcon);
        
        tvTitle.setText("Xác nhận");
        tvMessage.setText("Bạn có chắc chắn muốn " + action + " tài khoản này?");
        
        if (user.isActive()) {
            icon.setImageResource(R.drawable.ic_delete); // Vô hiệu hóa
            icon.setColorFilter(Color.parseColor("#EF4444"));
        } else {
            icon.setImageResource(R.drawable.ic_restore); // Kích hoạt lại
            icon.setColorFilter(Color.parseColor("#10B981"));
        }
        
        dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDialogConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            toggleUserStatus(user, position);
        });
        
        dialog.show();
    }

    private void toggleUserStatus(AdminUserDto user, int position) {
        com.example.pemomovie.api.ApiService apiService = com.example.pemomovie.api.ApiClient.getClient().create(com.example.pemomovie.api.ApiService.class);
        apiService.toggleUserStatus(user.getId()).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    user.setActive(!user.isActive());
                    notifyItemChanged(position);
                    android.widget.Toast.makeText(context, "Đã cập nhật trạng thái", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                android.widget.Toast.makeText(context, "Lỗi kết nối", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserEmail, txtTier, txtStatus;
        android.widget.ImageView imgAvatar;
        android.widget.ImageButton btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtTier = itemView.findViewById(R.id.txtTier);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
