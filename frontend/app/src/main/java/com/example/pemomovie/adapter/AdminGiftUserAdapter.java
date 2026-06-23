package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminUserDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminGiftUserAdapter extends RecyclerView.Adapter<AdminGiftUserAdapter.ViewHolder> {
    private List<AdminUserDto> users = new ArrayList<>();
    private Set<Long> selectedUserIds = new HashSet<>();
    private Context context;

    public AdminGiftUserAdapter(Context context) {
        this.context = context;
    }

    public void setUsers(List<AdminUserDto> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public List<Long> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user_gift, parent, false);
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
            holder.lblPremiumStatus.setText("PREMIUM");
            holder.lblPremiumStatus.setTextColor(Color.parseColor("#F59E0B")); // Gold
            holder.lblPremiumStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4DF59E0B"))); // Translucent Gold
        } else {
            holder.lblPremiumStatus.setText("FREE");
            holder.lblPremiumStatus.setTextColor(Color.parseColor("#9CA3AF")); // Gray
            holder.lblPremiumStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4D9CA3AF"))); // Translucent Gray
        }

        // Prevent check listener loop
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedUserIds.contains(user.getId()));
        
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUserIds.add(user.getId());
            } else {
                selectedUserIds.remove(user.getId());
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserEmail, lblPremiumStatus;
        android.widget.ImageView imgAvatar;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            lblPremiumStatus = itemView.findViewById(R.id.lblPremiumStatus);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
