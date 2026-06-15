// WatchingAdapter.java
package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;

public class WatchingAdapter extends RecyclerView.Adapter<WatchingAdapter.ViewHolder> {
    private int itemCount = 2; // Số lượng item giả định để test layout

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watching_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: Gán dữ liệu cho từng bộ phim đang xem ở đây (tên phim, tập, progress...)
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
