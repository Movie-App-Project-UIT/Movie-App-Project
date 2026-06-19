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
import com.example.pemomovie.mockdata.UserMock;
import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private List<UserMock> users = new ArrayList<>();
    private Context context;

    public AdminUserAdapter(Context context) {
        this.context = context;
    }

    public void setUsers(List<UserMock> users) {
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
        UserMock user = users.get(position);
        holder.txtUserName.setText(user.getName());
        holder.txtUserEmail.setText(user.getEmail());

        if (user.isPremium()) {
            holder.lblPremiumStatus.setText("PREMIUM");
            holder.lblPremiumStatus.setTextColor(Color.parseColor("#F59E0B")); // Gold
            holder.lblPremiumStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4DF59E0B"))); // Translucent Gold
        } else {
            holder.lblPremiumStatus.setText("FREE");
            holder.lblPremiumStatus.setTextColor(Color.parseColor("#9CA3AF")); // Gray
            holder.lblPremiumStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4D9CA3AF"))); // Translucent Gray
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserEmail, lblPremiumStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            lblPremiumStatus = itemView.findViewById(R.id.lblPremiumStatus);
        }
    }
}
