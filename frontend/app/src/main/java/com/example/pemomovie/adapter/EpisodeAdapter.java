package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pemomovie.R;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private final Context context;
    private final List<String> episodeList;
    private int selectedPosition = 0; // Default selected is 0

    public EpisodeAdapter(Context context, List<String> episodeList) {
        this.context = context;
        this.episodeList = episodeList;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        holder.btnEpisode.setText(episodeList.get(position));

        if (position == selectedPosition) {
            holder.btnEpisode.setBackgroundResource(R.drawable.bg_gradient_save_button);
            holder.btnEpisode.setTextColor(Color.WHITE);
        } else {
            holder.btnEpisode.setBackgroundColor(Color.parseColor("#333333"));
            holder.btnEpisode.setTextColor(Color.WHITE);
        }

        holder.btnEpisode.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
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
