package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.dto.NotificationDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationDto> notificationList;
    private Context context;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationDto notification);
        void onActionClick(NotificationDto notification);
    }

    public NotificationAdapter(Context context, List<NotificationDto> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public void updateData(List<NotificationDto> newData) {
        this.notificationList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_generic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationDto notif = notificationList.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        
        if (notif.getCreatedAt() != null) {
            holder.tvTime.setText(getTimeAgo(notif.getCreatedAt()));
        } else {
            holder.tvTime.setText("Vừa xong");
        }

        // Đọc / Chưa đọc
        holder.dotUnread.setVisibility(notif.getRead() != null && notif.getRead() ? View.INVISIBLE : View.VISIBLE);

        // Reset views
        holder.tvBadge.setVisibility(View.GONE);
        holder.btnAction.setVisibility(View.GONE);
        holder.ivArrow.setVisibility(View.GONE);

        // Setup UI based on type
        setupTypeSpecificUI(holder, notif);

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notif));
        holder.btnAction.setOnClickListener(v -> listener.onActionClick(notif));
    }

    private void setupTypeSpecificUI(ViewHolder holder, NotificationDto notif) {
        String type = notif.getType() != null ? notif.getType() : "SYSTEM";
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setColor(Color.parseColor("#161523"));
        borderDrawable.setCornerRadius(dpToPx(12));
        borderDrawable.setStroke(dpToPx(1), Color.parseColor("#33FFFFFF")); // Default border

        GradientDrawable badgeDrawable = new GradientDrawable();
        badgeDrawable.setColor(Color.TRANSPARENT);
        badgeDrawable.setCornerRadius(dpToPx(8));
        badgeDrawable.setStroke(dpToPx(1), Color.parseColor("#555555")); // Default badge border

        GradientDrawable btnDrawable = new GradientDrawable();
        btnDrawable.setColor(Color.parseColor("#2A1B4E"));
        btnDrawable.setCornerRadius(dpToPx(8));

        switch (type) {
            case "SUBSCRIPTION_EXPIRING":
                holder.ivIcon.setImageResource(R.drawable.ic_alarm_clock);
                holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_gradient_red);
                holder.ivIcon.setBackgroundTintList(null);
                holder.ivIcon.setColorFilter(Color.WHITE);
                
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#F44336")); // Red border
                
                holder.tvBadge.setText("Quan trọng");
                holder.tvBadge.setTextColor(Color.parseColor("#F44336"));
                badgeDrawable.setStroke(dpToPx(1), Color.parseColor("#F44336"));
                holder.tvBadge.setBackground(badgeDrawable);
                holder.tvBadge.setVisibility(View.VISIBLE);

                holder.btnAction.setText("Gia hạn ngay");
                holder.btnAction.setTextColor(Color.parseColor("#F44336"));
                btnDrawable.setColor(Color.parseColor("#3D1616")); // Dark red bg
                holder.btnAction.setBackground(btnDrawable);
                holder.btnAction.setVisibility(View.VISIBLE);
                break;
                
            case "SUBSCRIPTION_NEW_PLAN":
                holder.ivIcon.setImageResource(R.drawable.ic_crown);
                holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_gradient);
                holder.ivIcon.setColorFilter(Color.WHITE);
                
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#5424F2")); // Purple border
                
                holder.tvBadge.setText("VIP");
                holder.tvBadge.setTextColor(Color.parseColor("#B39DFF"));
                badgeDrawable.setStroke(dpToPx(1), Color.parseColor("#B39DFF"));
                holder.tvBadge.setBackground(badgeDrawable);
                holder.tvBadge.setVisibility(View.VISIBLE);

                holder.btnAction.setText("Xem đặc quyền");
                holder.btnAction.setTextColor(Color.parseColor("#B39DFF"));
                btnDrawable.setColor(Color.parseColor("#2A1B4E"));
                holder.btnAction.setBackground(btnDrawable);
                holder.btnAction.setVisibility(View.VISIBLE);
                break;

            case "GIFT_RECEIVED":
                holder.ivIcon.setImageResource(R.drawable.ic_gift);
                holder.ivIcon.setBackgroundResource(R.drawable.bg_gift_icon);
                holder.ivIcon.setColorFilter(Color.WHITE);
                
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#B38C22")); // Gold border
                
                holder.tvBadge.setText("Ưu đãi");
                holder.tvBadge.setTextColor(Color.parseColor("#F5D166"));
                badgeDrawable.setStroke(dpToPx(1), Color.parseColor("#F5D166"));
                holder.tvBadge.setBackground(badgeDrawable);
                holder.tvBadge.setVisibility(View.VISIBLE);

                if (notif.getIsClaimed() != null && notif.getIsClaimed()) {
                    holder.btnAction.setText("Đã kích hoạt");
                    holder.btnAction.setTextColor(Color.parseColor("#888888"));
                    btnDrawable.setColor(Color.parseColor("#333333")); // Dark gray bg
                    holder.btnAction.setBackground(btnDrawable);
                    holder.btnAction.setVisibility(View.VISIBLE);
                    holder.btnAction.setClickable(false);
                } else {
                    holder.btnAction.setText("Kích hoạt");
                    holder.btnAction.setTextColor(Color.parseColor("#F5D166"));
                    btnDrawable.setColor(Color.parseColor("#332A15")); // Dark gold bg
                    holder.btnAction.setBackground(btnDrawable);
                    holder.btnAction.setVisibility(View.VISIBLE);
                    holder.btnAction.setClickable(true);
                }
                break;
                
            case "REPLY":
                holder.ivIcon.setImageResource(R.drawable.ic_chat_bubble_dots);
                holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_gradient_pink);
                holder.ivIcon.setColorFilter(Color.WHITE);
                holder.ivArrow.setVisibility(View.VISIBLE);
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#4C1D95")); // Subtle purple border
                break;
                
            case "SYSTEM":
                holder.ivIcon.setImageResource(R.drawable.ic_megaphone);
                holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_gradient_blue);
                holder.ivIcon.setColorFilter(Color.WHITE);
                holder.ivArrow.setVisibility(View.VISIBLE);
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#1E3A8A")); // Subtle dark blue border
                break;
                
            default:
                holder.ivIcon.setImageResource(R.drawable.ic_notification);
                android.graphics.drawable.GradientDrawable defaultIconBg = new android.graphics.drawable.GradientDrawable();
                defaultIconBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                defaultIconBg.setColor(Color.parseColor("#4B5563"));
                holder.ivIcon.setBackground(defaultIconBg);
                holder.ivIcon.setColorFilter(Color.parseColor("#E5E7EB"));
                holder.ivArrow.setVisibility(View.VISIBLE);
                borderDrawable.setStroke(dpToPx(1), Color.parseColor("#374151"));
                break;
        }

        holder.layoutContainer.setBackground(borderDrawable);
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private String getTimeAgo(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) return "Vừa xong";
        try {
            // Remove fractional seconds if present
            if (createdAt.contains(".")) {
                createdAt = createdAt.substring(0, createdAt.indexOf("."));
            }
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(createdAt);
            if (date == null) return "Vừa xong";

            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            if (diff < 0) diff = 0;

            if (diff < 60 * 1000) {
                return "Vừa xong";
            } else if (diff < 60 * 60 * 1000) {
                return (diff / (60 * 1000)) + " phút trước";
            } else if (diff < 24 * 60 * 60 * 1000) {
                return (diff / (60 * 60 * 1000)) + " giờ trước";
            } else {
                return (diff / (24 * 60 * 60 * 1000)) + " ngày trước";
            }
        } catch (Exception e) {
            return "Vừa xong";
        }
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View dotUnread;
        LinearLayout layoutContainer;
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime, tvBadge, btnAction;
        ImageView ivArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dotUnread = itemView.findViewById(R.id.dotUnread);
            layoutContainer = itemView.findViewById(R.id.layoutContainer);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            btnAction = itemView.findViewById(R.id.btnAction);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}
