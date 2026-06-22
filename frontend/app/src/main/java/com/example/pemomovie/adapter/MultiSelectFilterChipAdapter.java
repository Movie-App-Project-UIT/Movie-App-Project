package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.FilterOption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectFilterChipAdapter extends RecyclerView.Adapter<MultiSelectFilterChipAdapter.ViewHolder> {

    private List<FilterOption> items;
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Set<FilterOption> selectedOptions);
    }

    public MultiSelectFilterChipAdapter(List<FilterOption> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.selectedPositions.add(0); // Default "Tất cả" is selected
    }

    public void updateData(List<FilterOption> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void selectByName(String name) {
        if (items == null) return;
        for (int i = 0; i < items.size(); i++) {
            if (name.equalsIgnoreCase(items.get(i).getName())) {
                selectedPositions.remove(0); // remove "Tất cả"
                selectedPositions.add(i);
                notifyDataSetChanged();
                triggerListener();
                break;
            }
        }
    }

    public void clearSelection() {
        selectedPositions.clear();
        selectedPositions.add(0);
        notifyDataSetChanged();
        triggerListener();
    }

    private void triggerListener() {
        if (listener != null) {
            Set<FilterOption> selectedOptions = new HashSet<>();
            for (int pos : selectedPositions) {
                selectedOptions.add(items.get(pos));
            }
            listener.onItemClick(selectedOptions);
        }
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

        if (selectedPositions.contains(position)) {
            holder.tvName.setBackgroundResource(R.drawable.bg_chip_selected_green);
            holder.tvName.setTextColor(0xFFFFFFFF); // White
        } else {
            holder.tvName.setBackgroundResource(R.drawable.bg_genres_default);
            holder.tvName.setTextColor(0xFF888888); // Gray
        }

        holder.itemView.setOnClickListener(v -> {
            if (position == 0) {
                // If "Tất cả" is clicked, clear others
                selectedPositions.clear();
                selectedPositions.add(0);
            } else {
                // If a specific genre is clicked, remove "Tất cả"
                selectedPositions.remove(0);
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position);
                    // If no items are selected, select "Tất cả"
                    if (selectedPositions.isEmpty()) {
                        selectedPositions.add(0);
                    }
                } else {
                    selectedPositions.add(position);
                }
            }
            notifyDataSetChanged();
            triggerListener();
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
