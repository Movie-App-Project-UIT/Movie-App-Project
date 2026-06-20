package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.FilterOption;

import java.util.List;

public class FilterChipAdapter extends RecyclerView.Adapter<FilterChipAdapter.ViewHolder> {

    private List<FilterOption> items;
    private int selectedPosition = 0; // Default "Tất cả" is selected
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FilterOption option);
    }

    public FilterChipAdapter(List<FilterOption> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<FilterOption> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_chip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilterOption option = items.get(position);
        holder.tvName.setText(option.getName());

        if (selectedPosition == position) {
            holder.tvName.setBackgroundResource(R.drawable.bg_chip_selected_green);
            holder.tvName.setTextColor(0xFFFFFFFF); // White
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_genres_default);
            holder.tvName.setTextColor(0xFF888888); // Gray
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onItemClick(option);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChipName);
        }
    }
}
