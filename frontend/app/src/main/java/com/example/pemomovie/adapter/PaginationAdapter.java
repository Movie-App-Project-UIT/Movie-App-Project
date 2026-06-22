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

import java.util.ArrayList;
import java.util.List;

public class PaginationAdapter extends RecyclerView.Adapter<PaginationAdapter.PaginationViewHolder> {

    private final Context context;
    private int totalPages;
    private int currentPage = 0; // 0-indexed internally
    private OnPageClickListener listener;
    private List<PageItem> pageItems = new ArrayList<>();

    public interface OnPageClickListener {
        void onPageClick(int page);
    }

    public static class PageItem {
        public int pageIndex; // -1 for ellipsis
        public String text;
        public boolean isCurrent;

        public PageItem(int pageIndex, String text, boolean isCurrent) {
            this.pageIndex = pageIndex;
            this.text = text;
            this.isCurrent = isCurrent;
        }
    }

    public PaginationAdapter(Context context, int totalPages, OnPageClickListener listener) {
        this.context = context;
        this.totalPages = totalPages;
        this.listener = listener;
        generatePageItems();
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        generatePageItems();
        notifyDataSetChanged();
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        generatePageItems();
        notifyDataSetChanged();
    }

    private void generatePageItems() {
        pageItems.clear();
        if (totalPages <= 7) {
            for (int i = 0; i < totalPages; i++) {
                pageItems.add(new PageItem(i, String.valueOf(i + 1), i == currentPage));
            }
        } else {
            if (currentPage < 4) {
                for (int i = 0; i < 5; i++) {
                    pageItems.add(new PageItem(i, String.valueOf(i + 1), i == currentPage));
                }
                pageItems.add(new PageItem(-1, "...", false));
                pageItems.add(new PageItem(totalPages - 1, String.valueOf(totalPages), false));
            } else if (currentPage >= totalPages - 4) {
                pageItems.add(new PageItem(0, "1", false));
                pageItems.add(new PageItem(-1, "...", false));
                for (int i = totalPages - 5; i < totalPages; i++) {
                    pageItems.add(new PageItem(i, String.valueOf(i + 1), i == currentPage));
                }
            } else {
                pageItems.add(new PageItem(0, "1", false));
                pageItems.add(new PageItem(-1, "...", false));
                pageItems.add(new PageItem(currentPage - 1, String.valueOf(currentPage), false));
                pageItems.add(new PageItem(currentPage, String.valueOf(currentPage + 1), true));
                pageItems.add(new PageItem(currentPage + 1, String.valueOf(currentPage + 2), false));
                pageItems.add(new PageItem(-1, "...", false));
                pageItems.add(new PageItem(totalPages - 1, String.valueOf(totalPages), false));
            }
        }
    }

    @NonNull
    @Override
    public PaginationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pagination, parent, false);
        return new PaginationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaginationViewHolder holder, int position) {
        PageItem item = pageItems.get(position);
        holder.tvPageNumber.setText(item.text);

        if (item.pageIndex == -1) {
            // Ellipsis
            holder.tvPageNumber.setBackgroundColor(Color.TRANSPARENT);
            holder.tvPageNumber.setTextColor(Color.WHITE);
            holder.tvPageNumber.setOnClickListener(null);
        } else {
            if (item.isCurrent) {
                holder.tvPageNumber.setBackgroundResource(R.drawable.bg_gradient_save_button);
                holder.tvPageNumber.setTextColor(Color.WHITE);
            } else {
                holder.tvPageNumber.setBackgroundResource(R.drawable.bg_episode_unselected);
                holder.tvPageNumber.setTextColor(Color.WHITE);
            }

            holder.tvPageNumber.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPageClick(item.pageIndex);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return pageItems.size();
    }

    public static class PaginationViewHolder extends RecyclerView.ViewHolder {
        TextView tvPageNumber;

        public PaginationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPageNumber = itemView.findViewById(R.id.tvPageNumber);
        }
    }
}
