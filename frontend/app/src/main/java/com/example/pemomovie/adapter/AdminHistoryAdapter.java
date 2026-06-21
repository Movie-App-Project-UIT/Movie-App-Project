package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminHistoryDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminHistoryAdapter extends RecyclerView.Adapter<AdminHistoryAdapter.ViewHolder> {

    private final Context context;
    private List<AdminHistoryDto> historyList = new ArrayList<>();
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AdminHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setHistories(List<AdminHistoryDto> histories) {
        this.historyList = histories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminHistoryDto history = historyList.get(position);
        
        holder.tvDetails.setText(history.getDetails() != null ? history.getDetails() : "Không có chi tiết");
        holder.tvAdminEmail.setText(history.getAdminEmail() != null ? history.getAdminEmail() : "Unknown");
        
        try {
            if (history.getCreatedAt() != null) {
                Date date = inputFormat.parse(history.getCreatedAt());
                holder.tvCreatedAt.setText(date != null ? outputFormat.format(date) : history.getCreatedAt());
            } else {
                holder.tvCreatedAt.setText("");
            }
        } catch (ParseException e) {
            holder.tvCreatedAt.setText(history.getCreatedAt());
        }

        // Set Icon and colors based on Action Type
        String actionType = history.getActionType();
        if (actionType == null) actionType = "";
        
        int iconRes = R.drawable.ic_history;
        String colorStr = "#9CA3AF"; // default gray
        
        switch (actionType.toUpperCase()) {
            case "CREATE":
                iconRes = R.drawable.ic_add;
                colorStr = "#10B981"; // Green
                break;
            case "UPDATE":
                iconRes = R.drawable.ic_edit;
                colorStr = "#F59E0B"; // Orange
                break;
            case "DELETE":
                iconRes = R.drawable.ic_delete;
                colorStr = "#EF4444"; // Red
                break;
            case "RESTORE":
                iconRes = R.drawable.ic_restore; // we might not have ic_restore, fallback
                colorStr = "#3B82F6"; // Blue
                break;
            case "GIFT":
                iconRes = R.drawable.ic_star;
                colorStr = "#D946EF"; // Pink
                break;
        }
        
        holder.ivActionIcon.setImageResource(iconRes);
        holder.ivActionIcon.setColorFilter(Color.parseColor(colorStr));
        // You could also tint the background circle if you want
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivActionIcon;
        TextView tvDetails, tvAdminEmail, tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActionIcon = itemView.findViewById(R.id.ivActionIcon);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAdminEmail = itemView.findViewById(R.id.tvAdminEmail);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}
