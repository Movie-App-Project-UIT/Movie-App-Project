package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;
import com.example.pemomovie.dto.EpisodeDto;

import java.util.List;

public class AdminEpisodeAdapter extends RecyclerView.Adapter<AdminEpisodeAdapter.EpisodeViewHolder> {

    private final Context context;
    private final List<EpisodeDto> episodeList;
    private int selectedPosition = -1;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(EpisodeDto episode, int position);
    }

    public AdminEpisodeAdapter(Context context, List<EpisodeDto> episodeList, OnItemClickListener listener) {
        this.context = context;
        this.episodeList = episodeList;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        EpisodeDto episode = episodeList.get(position);
        
        String text = episode.getEpisodeNumber() != null ? "Tập " + episode.getEpisodeNumber() : "Tập mới";
        holder.btnEpisode.setText(text);

        if (position == selectedPosition) {
            holder.btnEpisode.setBackgroundResource(R.drawable.bg_gradient_save_button);
            holder.btnEpisode.setTextColor(Color.WHITE);
        } else {
            holder.btnEpisode.setBackgroundResource(R.drawable.bg_episode_unselected);
            if (episode.isDeleted()) {
                holder.btnEpisode.setTextColor(Color.parseColor("#EF4444")); // Red if deleted
            } else if (episode.isPremium()) {
                holder.btnEpisode.setTextColor(Color.parseColor("#EAB308")); // Yellow if premium
            } else {
                holder.btnEpisode.setTextColor(Color.WHITE);
            }
        }

        holder.btnEpisode.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(episode, holder.getAdapterPosition());
            }
            setSelectedPosition(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return episodeList != null ? episodeList.size() : 0;
    }

    public static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        androidx.appcompat.widget.AppCompatButton btnEpisode;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            btnEpisode = itemView.findViewById(R.id.btnEpisode);
        }
    }
}
