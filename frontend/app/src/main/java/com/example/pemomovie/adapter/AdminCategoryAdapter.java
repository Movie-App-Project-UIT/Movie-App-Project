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
import com.example.pemomovie.dto.AdminGenreDto;
import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {
    private List<AdminGenreDto> categories = new ArrayList<>();
    private Context context;
    private OnCategoryActionListener actionListener;

    public interface OnCategoryActionListener {
        void onEdit(AdminGenreDto category);
        void onDelete(AdminGenreDto category);
    }

    public AdminCategoryAdapter(Context context, OnCategoryActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
    }

    public void setCategories(List<AdminGenreDto> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminGenreDto category = categories.get(position);
        holder.txtCategoryName.setText(category.getName());
        
        holder.txtViews.setText(formatNumber(category.getViewCount()) + " lượt xem");
        holder.txtMediaCount.setText(category.getMediaCount() + " phim");

        try {
            holder.colorIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(category.getColorCode())));
        } catch (Exception e) {
            // default
        }

        // Nếu isDeleted = true thì đổi màu nút Delete thành màu xanh (Khôi phục)
        if (category.isDeleted()) {
            holder.btnDelete.setImageResource(R.drawable.ic_restore);
            holder.btnDelete.setColorFilter(Color.parseColor("#10B981")); // Màu xanh
            ((com.google.android.material.card.MaterialCardView) holder.btnDelete.getParent()).setStrokeColor(Color.parseColor("#10B981"));
            
            // Làm mờ text
            holder.txtCategoryName.setTextColor(Color.parseColor("#6B7280"));
        } else {
            holder.btnDelete.setImageResource(R.drawable.ic_delete);
            holder.btnDelete.setColorFilter(Color.parseColor("#EF4444")); // Màu đỏ
            ((com.google.android.material.card.MaterialCardView) holder.btnDelete.getParent()).setStrokeColor(Color.parseColor("#EF4444"));
            
            // Trả lại text sáng
            holder.txtCategoryName.setTextColor(Color.WHITE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(category);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(category);
        });
    }

    private String formatNumber(int number) {
        if (number >= 1000000) return String.format("%.1fM", number / 1000000.0);
        if (number >= 1000) return String.format("%.1fK", number / 1000.0);
        return String.valueOf(number);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View colorIndicator;
        TextView txtCategoryName, txtViews, txtMediaCount;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtViews = itemView.findViewById(R.id.txtViews);
            txtMediaCount = itemView.findViewById(R.id.txtMediaCount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
