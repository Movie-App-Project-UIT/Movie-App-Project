package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminMovieSaveRequest;

import java.util.ArrayList;
import java.util.List;

public class AdminSubtitleAdapter extends RecyclerView.Adapter<AdminSubtitleAdapter.ViewHolder> {
    private List<AdminMovieSaveRequest.AdminSubtitleRequest> subtitles = new ArrayList<>();
    private final OnSubtitleActionListener actionListener;

    public interface OnSubtitleActionListener {
        void onDelete(int position);
    }

    public AdminSubtitleAdapter(OnSubtitleActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setSubtitles(List<AdminMovieSaveRequest.AdminSubtitleRequest> subtitles) {
        this.subtitles = subtitles;
        notifyDataSetChanged();
    }

    public List<AdminMovieSaveRequest.AdminSubtitleRequest> getSubtitles() {
        return subtitles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_subtitle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminMovieSaveRequest.AdminSubtitleRequest subtitle = subtitles.get(position);
        holder.tvSubtitleLanguage.setText(subtitle.getLanguage());
        holder.tvSubtitleUrl.setText(subtitle.getFileUrl());
        
        holder.btnDeleteSubtitle.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subtitles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubtitleLanguage, tvSubtitleUrl;
        ImageView btnDeleteSubtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubtitleLanguage = itemView.findViewById(R.id.tvSubtitleLanguage);
            tvSubtitleUrl = itemView.findViewById(R.id.tvSubtitleUrl);
            btnDeleteSubtitle = itemView.findViewById(R.id.btnDeleteSubtitle);
        }
    }
}
