package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminSubscriptionDto;
import com.example.pemomovie.ui.admin.AdminSubscriptionDetailActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSubscriptionAdapter extends RecyclerView.Adapter<AdminSubscriptionAdapter.SubscriptionViewHolder> {

    private Context context;
    private List<AdminSubscriptionDto> subscriptionList = new ArrayList<>();

    public AdminSubscriptionAdapter(Context context) {
        this.context = context;
    }

    public void setSubscriptions(List<AdminSubscriptionDto> subscriptions) {
        this.subscriptionList = subscriptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        AdminSubscriptionDto subscription = subscriptionList.get(position);

        holder.txtSubscriptionName.setText(subscription.getName());
        holder.txtDuration.setText(subscription.getDurationDays() + " ngày");
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.txtPrice.setText(format.format(subscription.getPrice()));

        if (subscription.getIsActive()) {
            holder.imgStatus.setImageResource(R.drawable.ic_check);
            holder.imgStatus.setColorFilter(android.graphics.Color.parseColor("#10B981"));
            holder.btnStatus.setStrokeColor(android.graphics.Color.parseColor("#10B981"));
            holder.colorIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#8B5CF6")));
        } else {
            holder.imgStatus.setImageResource(R.drawable.ic_close);
            holder.imgStatus.setColorFilter(android.graphics.Color.parseColor("#EF4444"));
            holder.btnStatus.setStrokeColor(android.graphics.Color.parseColor("#EF4444"));
            holder.colorIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4B5563")));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminSubscriptionDetailActivity.class);
            intent.putExtra("SUBSCRIPTION_ID", subscription.getId());
            intent.putExtra("SUBSCRIPTION_IS_ACTIVE", subscription.getIsActive());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList != null ? subscriptionList.size() : 0;
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView txtSubscriptionName, txtPrice, txtDuration;
        View colorIndicator;
        ImageView imgStatus;
        com.google.android.material.card.MaterialCardView btnStatus;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSubscriptionName = itemView.findViewById(R.id.txtSubscriptionName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            btnStatus = itemView.findViewById(R.id.btnStatus);
        }
    }
}
